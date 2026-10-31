import { ArrowDown, ArrowRight, ArrowUpRight, Check, ChevronDown, CreditCard, Leaf, Menu, MoveUpRight, Wallet } from "lucide-react";
import { useState } from "react";
import { Button } from "@/shared/ui/button";

const activity = [
  { initials: "L", name: "Linear", detail: "Team software", amount: "−€24.00", color: "bg-violet-100 text-violet-800" },
  { initials: "A", name: "Airbnb", detail: "Travel · Sam", amount: "−€284.50", color: "bg-rose-100 text-rose-700" },
  { initials: "F", name: "Fieldwork Studio", detail: "Client payment", amount: "+€2,450.00", color: "bg-emerald-100 text-emerald-800" },
];

function Wordmark({ light = false }: { light?: boolean }) {
  return <a href="#top" className={`inline-flex items-center gap-2 font-display text-2xl font-bold tracking-[-0.08em] ${light ? "text-white" : "text-[#1d3024]"}`} aria-label="Jexis home"><span className={`grid size-8 place-items-center rounded-[10px_10px_10px_3px] ${light ? "bg-[#c8e0b4] text-[#23402d]" : "bg-[#294d34] text-white"}`}><Leaf size={18} /></span>jexis</a>;
}

function SiteHeader() {
  const [open, setOpen] = useState(false);
  return (
    <header className="relative z-10 mx-auto flex max-w-7xl items-center justify-between px-5 py-5 md:px-10 lg:px-14">
      <Wordmark />
      <button className="grid size-10 place-items-center rounded-md border border-[#dfe3dc] text-[#294d34] md:hidden" onClick={() => setOpen(!open)} aria-label={open ? "Close navigation" : "Open navigation"}>{open ? "×" : <Menu size={19} />}</button>
      <nav className={`${open ? "flex" : "hidden"} absolute left-4 right-4 top-[76px] flex-col gap-1 rounded-lg border border-[#e2e5df] bg-[#fbfaf7] p-3 shadow-lg md:static md:flex md:flex-row md:items-center md:gap-8 md:border-0 md:bg-transparent md:p-0 md:shadow-none`}>
        <a onClick={() => setOpen(false)} className="px-3 py-2 text-sm text-[#555f56] hover:text-[#294d34]" href="#product">Product</a>
        <a onClick={() => setOpen(false)} className="px-3 py-2 text-sm text-[#555f56] hover:text-[#294d34]" href="#how-it-works">How it works</a>
        <a onClick={() => setOpen(false)} className="px-3 py-2 text-sm text-[#555f56] hover:text-[#294d34]" href="#about">About Jexis</a>
        <div className="mt-2 flex items-center justify-between border-t border-[#e7e9e3] pt-3 md:hidden"><a className="px-3 text-sm font-semibold text-[#294d34]" href="#get-started">Log in</a><Button href="#get-started" size="small">Get started <ArrowRight size={14} /></Button></div>
      </nav>
      <div className="hidden items-center gap-6 md:flex"><a className="text-sm font-semibold text-[#405144] hover:text-[#294d34]" href="#get-started">Log in</a><Button href="#get-started" size="small">Get started <ArrowRight size={14} /></Button></div>
    </header>
  );
}

function AccountSnapshot() {
  return (
    <div className="relative mx-auto w-full max-w-[510px]">
      <div className="absolute -right-5 -top-8 h-36 w-36 rounded-full bg-[#d9e6ce] blur-3xl md:-right-12 md:-top-12 md:h-56 md:w-56" />
      <div className="relative overflow-hidden rounded-xl border border-[#e4e7df] bg-white shadow-[0_24px_70px_-35px_rgba(31,53,35,.35)]">
        <div className="flex items-center justify-between border-b border-[#eef0eb] px-5 py-4"><div className="flex items-center gap-2 text-xs font-semibold text-[#455247]"><span className="grid size-7 place-items-center rounded-md bg-[#edf2e9] text-[#446446]"><Wallet size={15} /></span> Studio North <ChevronDown size={13} className="text-[#92998f]" /></div><span className="flex items-center gap-2 text-[10px] text-[#778276]"><i className="size-1.5 rounded-full bg-[#6c9a65]" /> Updated just now</span></div>
        <div className="px-5 pb-5 pt-6 sm:px-7"><p className="text-xs text-[#818980]">Available across your accounts</p><div className="mt-1 flex items-end justify-between gap-3"><strong className="font-display text-3xl font-semibold tracking-[-0.06em] text-[#1e3024] sm:text-4xl">€48,290<span className="text-xl text-[#738173]">.00</span></strong><span className="mb-1 inline-flex items-center gap-1 rounded-full bg-[#edf4e9] px-2 py-1 text-[10px] font-semibold text-[#4c754d]"><ArrowUpRight size={12} /> 12.8%</span></div>
          <div className="mt-7 flex h-24 items-end gap-1.5 border-b border-[#edf0ea] sm:h-28">{[28, 37, 32, 53, 43, 67, 58, 46, 77, 63, 86, 70, 94, 75, 88, 100, 78, 92, 71, 83, 66, 91, 75, 97].map((height, i) => <span key={i} className={`flex-1 rounded-t-[2px] ${i === 15 ? "bg-[#66875f]" : "bg-[#dce8d5]"}`} style={{ height: `${height}%` }} />)}</div>
          <div className="mt-2 flex justify-between text-[9px] text-[#9ba198]"><span>Oct 01</span><span>Oct 08</span><span>Oct 15</span><span>Oct 22</span><span>Oct 31</span></div>
        </div>
        <div className="border-t border-[#eef0eb] bg-[#fcfcfa] px-5 py-3 sm:px-7"><div className="mb-1 flex items-center justify-between"><span className="text-[10px] font-semibold uppercase tracking-[.12em] text-[#939a90]">Latest activity</span><a href="#product" className="text-[10px] font-semibold text-[#476448]">All activity <ArrowRight size={11} className="inline" /></a></div>{activity.map((item) => <div key={item.name} className="flex items-center gap-3 border-b border-[#eff1ed] py-2.5 last:border-0"><span className={`grid size-7 place-items-center rounded-full text-[10px] font-bold ${item.color}`}>{item.initials}</span><span className="min-w-0 flex-1"><strong className="block text-[11px] font-semibold text-[#38443a]">{item.name}</strong><small className="mt-0.5 block text-[9px] text-[#90978e]">{item.detail}</small></span><strong className={`text-[11px] font-semibold ${item.amount.startsWith("+") ? "text-[#52734d]" : "text-[#3c453d]"}`}>{item.amount}</strong></div>)}</div>
      </div>
      <div className="absolute -bottom-5 -left-3 hidden items-center gap-2 rounded-lg border border-[#e5e9e1] bg-white px-3 py-2.5 shadow-lg sm:flex md:-left-10"><span className="grid size-7 place-items-center rounded-full bg-[#edf4e9] text-[#4c754d]"><Check size={15} /></span><span><b className="block text-[10px] text-[#38483a]">All accounts in sync</b><small className="mt-0.5 block text-[9px] text-[#92998f]">Your money, one clear view</small></span></div>
    </div>
  );
}

function Hero() {
  return <section className="mx-auto grid max-w-7xl items-center gap-12 px-5 pb-16 pt-9 md:grid-cols-[.9fr_1.1fr] md:gap-8 md:px-10 md:pb-24 md:pt-16 lg:px-14"><div><div className="mb-6 flex items-center gap-2 text-[10px] font-bold uppercase tracking-[.16em] text-[#627b60]"><span className="h-px w-6 bg-[#829b78]" /> Business finances in better balance</div><h1 className="max-w-xl font-display text-[clamp(3.25rem,6.5vw,5.4rem)] font-medium leading-[.97] tracking-[-.075em] text-[#1c3023]">Your money,<br /><span className="font-serif font-normal italic text-[#77936d]">on the same page.</span></h1><p className="mt-6 max-w-md text-[15px] leading-7 text-[#68736a]">Accounts, cards, and team spending in one considered place. So you can spend less time keeping up and more time moving forward.</p><div className="mt-7 flex flex-wrap items-center gap-4"><Button href="#get-started">Explore Jexis <ArrowRight size={16} /></Button><a href="#product" className="inline-flex items-center gap-2 text-sm font-semibold text-[#425846] hover:text-[#294d34]">Take a look <MoveUpRight size={14} /></a></div><div className="mt-9 flex items-center gap-3 border-t border-[#e4e7df] pt-5"><span className="grid size-9 place-items-center rounded-full bg-[#e9efe3] text-[#55724e]"><Leaf size={17} /></span><p className="text-xs leading-5 text-[#6d776d]"><strong className="block font-semibold text-[#394b3d]">For teams doing good work.</strong>Clear, calm tools for growing businesses.</p></div></div><AccountSnapshot /></section>;
}

function ProductSection() {
  const items = [
    { icon: Wallet, number: "01", title: "A clearer view of cash", body: "See balances and activity across your business accounts without switching between tabs.", detail: "Accounts, together" },
    { icon: CreditCard, number: "02", title: "Team spending with context", body: "Give people the room to get things done, with cards and limits that make sense for your team.", detail: "Cards, in your control" },
    { icon: ArrowDown, number: "03", title: "Fewer loose ends", body: "Understand what’s coming in and going out, then make the next decision with confidence.", detail: "A simpler daily rhythm" },
  ];
  return <section className="border-y border-[#e4e7df] bg-[#f3f4ee]" id="product"><div className="mx-auto max-w-7xl px-5 py-16 md:px-10 md:py-24 lg:px-14"><div className="grid gap-8 md:grid-cols-[.8fr_1.2fr] md:gap-16"><div><div className="flex items-center gap-2 text-[10px] font-bold uppercase tracking-[.16em] text-[#627b60]"><span className="h-px w-6 bg-[#829b78]" /> One place to start</div><h2 className="mt-5 max-w-md font-display text-4xl font-medium leading-[1.05] tracking-[-.06em] text-[#1d3024] md:text-5xl">A more human way to handle business money.</h2><p className="mt-5 max-w-sm text-sm leading-6 text-[#6b756c]">The everyday essentials, brought together with enough clarity to help you focus on the work that matters.</p><a className="mt-6 inline-flex items-center gap-2 text-sm font-semibold text-[#35573a]" href="#get-started">See what Jexis can do <ArrowRight size={15} /></a></div><div className="divide-y divide-[#dfe4db]">{items.map(({ icon: Icon, number, title, body, detail }) => <article key={number} className="grid gap-x-5 gap-y-3 py-5 first:pt-0 last:pb-0 sm:grid-cols-[40px_1fr_auto] sm:items-start"><span className="grid size-9 place-items-center rounded-lg border border-[#dfe5da] bg-[#fbfaf7] text-[#49664a]"><Icon size={17} /></span><div><div className="mb-1 text-[10px] font-semibold tracking-[.12em] text-[#94a08f]">{number}</div><h3 className="font-display text-lg font-semibold tracking-[-.03em] text-[#2a3b2e]">{title}</h3><p className="mt-1 max-w-lg text-xs leading-5 text-[#737d73]">{body}</p></div><span className="hidden items-center gap-1.5 pt-1 text-[10px] font-semibold text-[#718270] sm:inline-flex"><span className="size-1.5 rounded-full bg-[#89a17e]" />{detail}</span></article>)}</div></div></div></section>;
}

function ClosingSection() {
  return <section id="get-started" className="mx-auto flex max-w-7xl flex-col gap-7 px-5 py-16 md:flex-row md:items-end md:justify-between md:px-10 md:py-20 lg:px-14"><div><div className="flex items-center gap-2 text-[10px] font-bold uppercase tracking-[.16em] text-[#627b60]"><span className="h-px w-6 bg-[#829b78]" /> A better balance starts here</div><h2 className="mt-4 font-display text-4xl font-medium tracking-[-.06em] text-[#1d3024] md:text-5xl">Make room for better.</h2><p className="mt-3 text-sm text-[#707a70]">Bring your business finances into focus with Jexis.</p></div><div className="flex flex-col items-start gap-3"><Button href="mailto:hello@jexis.com">Talk to our team <ArrowRight size={16} /></Button><span className="text-[10px] text-[#818a80]">A real person will get back to you.</span></div></section>;
}

export default function LandingPage() {
  return <main id="top" className="min-h-screen bg-[#fbfaf7] text-[#202520]"><div className="border-b border-[#e8ebe4] bg-[#f1f3ed] px-4 py-2 text-center text-[11px] text-[#59675a]">An easier way to see where your business stands. <a href="#product" className="ml-1 inline-flex items-center gap-1 font-semibold text-[#35573a]">Meet Jexis <ArrowRight size={12} /></a></div><SiteHeader /><Hero /><ProductSection /><section id="how-it-works" className="bg-[#1d3a2c] px-5 py-16 text-center text-white md:py-20"><div className="mx-auto max-w-3xl"><Leaf className="mx-auto text-[#c8e0b4]" size={25} /><p className="mt-5 font-display text-2xl leading-snug tracking-[-.035em] md:text-4xl">Good finances shouldn’t take over your day. They should <span className="font-serif italic text-[#c8e0b4]">help you get on with it.</span></p><a href="#get-started" className="mt-6 inline-flex items-center gap-2 text-xs font-semibold text-[#e0ebd8]">How Jexis works <MoveUpRight size={13} /></a></div></section><section id="about" className="sr-only">Jexis helps businesses bring accounts, cards, and spending into one clear view.</section><ClosingSection /><footer className="border-t border-[#e4e7df] px-5 py-6 md:px-10 lg:px-14"><div className="mx-auto flex max-w-7xl flex-col gap-4 sm:flex-row sm:items-center"><Wordmark /><span className="text-xs text-[#7b847a]">Business finances in better balance.</span><div className="flex gap-5 text-xs font-medium text-[#687368] sm:ml-auto"><a href="#product">Product</a><a href="#about">About</a><a href="mailto:hello@jexis.com">Contact</a></div><small className="text-[10px] text-[#91988f]">© 2026 Jexis</small></div></footer></main>;
}
