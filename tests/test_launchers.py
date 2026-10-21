"""Run with python3 -m unittest discover -s tests; no daemon or real secrets needed."""
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]


class LaunchersTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        shutil.copytree(ROOT / 'scripts', self.root / 'scripts')
        for mode in ('dev', 'prod'):
            shutil.copy(ROOT / f'.env.{mode}.example', self.root / f'.env.{mode}')
            shutil.copy(ROOT / f'compose.{mode}.yml', self.root / f'compose.{mode}.yml')

    def run_shell(self, command, **env):
        return subprocess.run(['bash', '-c', 'source scripts/common.sh; ' + command],
                              cwd=self.root, env={**os.environ, **env}, text=True,
                              stdout=subprocess.PIPE, stderr=subprocess.PIPE)

    def test_literal_secrets_are_not_evaluated(self):
        value = r'pass$word#hash\slash=$(touch SHOULD_NOT_EXIST)'
        (self.root / '.env.dev').write_text('DB_PASSWORD=' + value + '\n')
        result = self.run_shell('load_environment dev; printf "%s" "$DB_PASSWORD"')
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(result.stdout, value)
        self.assertFalse((self.root / 'SHOULD_NOT_EXIST').exists())

    def test_duplicate_and_quoted_values_are_rejected(self):
        for contents in ('DB_NAME=a\nDB_NAME=b\n', 'DB_PASSWORD="quoted"\n'):
            (self.root / '.env.dev').write_text(contents)
            self.assertNotEqual(self.run_shell('load_environment dev').returncode, 0)

    def test_missing_file_and_missing_credentials_fail(self):
        (self.root / '.env.dev').unlink()
        self.assertNotEqual(self.run_shell('load_environment dev').returncode, 0)
        (self.root / '.env.dev').write_text('DB_NAME=test\n')
        result = self.run_shell('load_environment dev; require_config', DB_URL='inherited')
        self.assertNotEqual(result.returncode, 0)
        self.assertIn('DB_URL', result.stderr)

    def test_profile_is_selected_by_command(self):
        result = self.run_shell('load_environment dev; printf "%s" "$SPRING_PROFILES_ACTIVE"',
                                SPRING_PROFILES_ACTIVE='prod')
        self.assertEqual(result.stdout, 'dev')

    def test_dev_launches_gradle_from_backend(self):
        env_file = self.root / '.env.dev'
        env_file.write_text(env_file.read_text().replace('CHANGE_ME', 'test-value'))
        backend = self.root / 'backend'
        backend.mkdir()
        wrapper = backend / 'gradlew'
        wrapper.write_text('#!/usr/bin/env bash\nprintf "%s\\n" "$PWD" "$@" "$SPRING_PROFILES_ACTIVE"\n')
        wrapper.chmod(0o755)
        fake_bin = self.root / 'bin'
        fake_bin.mkdir()
        for name in ('java', 'docker'):
            command = fake_bin / name
            command.write_text('#!/usr/bin/env bash\nexit 0\n')
            command.chmod(0o755)
        result = subprocess.run(
            ['bash', str(self.root / 'scripts/dev')], cwd=self.root.parent,
            env={**os.environ, 'PATH': str(fake_bin) + os.pathsep + os.environ['PATH']},
            text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        self.assertEqual(result.returncode, 0, result.stderr)
        output = result.stdout.splitlines()
        self.assertEqual(Path(output[-4]).resolve(), backend.resolve())
        self.assertEqual(output[-3:], ['bootRun', '--args=--spring.profiles.active=dev', 'dev'])

    @unittest.skipUnless(shutil.which('docker'), 'Docker Compose CLI required')
    def test_compose_isolation_and_literal_passwords(self):
        # Compose re-escapes dollar signs when serializing its canonical model.
        configs = {}
        for mode in ('dev', 'prod'):
            p = self.root / f'.env.{mode}'
            p.write_text(p.read_text().replace('DB_PASSWORD=CHANGE_ME', r'DB_PASSWORD=a$literal#hash\slash'))
            result = self.run_shell(f'load_environment {mode}; compose config --format json')
            self.assertEqual(result.returncode, 0, result.stderr)
            configs[mode] = json.loads(result.stdout)
            self.assertEqual(configs[mode]['services']['postgres']['environment']['POSTGRES_PASSWORD'].replace('$$', '$'),
                             r'a$literal#hash\slash')
        self.assertEqual(set(configs['dev']['services']), {'postgres', 'rabbitmq', 'redis'})
        self.assertNotEqual(configs['dev']['volumes']['postgres_data']['name'],
                            configs['prod']['volumes']['postgres_data']['name'])
        self.assertNotEqual(configs['dev']['volumes']['redis_data']['name'],
                            configs['prod']['volumes']['redis_data']['name'])
        for service in ('postgres', 'rabbitmq', 'redis', 'backend'):
            self.assertNotIn('ports', configs['prod']['services'][service])
        self.assertEqual(configs['prod']['services']['backend']['environment']['SPRING_PROFILES_ACTIVE'], 'prod')
        self.assertEqual(Path(configs['prod']['services']['backend']['build']['context']).resolve(),
                         (self.root / 'backend').resolve())
        self.assertEqual(configs['prod']['services']['backend']['environment']['DB_PASSWORD'].replace('$$', '$'), r'a$literal#hash\slash')


if __name__ == '__main__':
    unittest.main()
