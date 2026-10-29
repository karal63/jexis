# Email delivery

Activation user creation and password-reset token creation commit their email outbox
entries in the same database transaction. RabbitMQ outages do not roll back successful
registration: committed emails remain in `email_outbox` for publication after recovery.
The publisher locks up to 20 due rows with `FOR UPDATE SKIP LOCKED`, permitting multiple
application instances. Failures back off from 5 seconds to 5 minutes indefinitely.
Only a positive publisher confirmation without a mandatory return removes an entry.
Messages are persistent; all email queues and exchanges are durable.

SMTP errors propagate to the listener. It attempts delivery three times, with 1- and
2-second delays, then rejects without requeue. RabbitMQ routes the failed message to
`email.dead.exchange` / `email.dead.queue`. Alert on DLQ depth and the age of pending
outbox rows. After fixing the delivery failure, operators can replay DLQ messages to
`email.exchange` using `email.routing.key`; acknowledge the original only after a
confirmed replay. Do not automatically cycle the DLQ back to the delivery queue.

Delivery is at least once. A crash after broker acceptance but before outbox commit,
or after SMTP acceptance but before acknowledgement, can send duplicate mail. The
same activation/reset token is reused, and a stable message ID identifies outbox
replays. Outbox and DLQ bodies contain bearer tokens: restrict access and do not log
bodies. Successful publication deletes the outbox body. Monitor outages promptly:
password reset tokens still expire 30 minutes after creation.

## Deployment from the old email.queue

The new `email.delivery.queue` has dead-letter arguments that cannot be added by
redeclaring the old `email.queue`. Perform a coordinated cutover (no mixed old/new
application instances):

1. Pause signup/reset requests and stop old consumers/publishers.
2. Unbind `email.queue` from `email.exchange` for `email.routing.key`, preserving its
   queued messages. Never delete a nonempty old queue.
3. Deploy the application, applying Flyway V8. Startup declares the new delivery and
   dead-letter topology. Verify both bindings before resuming requests.
4. Move retained messages from the old queue into the new delivery queue using a
   RabbitMQ shovel with publisher-confirm acknowledgements; remove the old queue
   only once it is empty and the shovel has completed.

Do not leave the old queue bound: it would retain an extra copy of every new email.
Tests disable the scheduler with `application.email.outbox.enabled=false`; production
polls every 5000 ms, adjustable using `application.email.outbox.poll-delay`.
