import { Route, Routes } from "react-router-dom";
import LandingPage from "@/pages/landing/LandingPage.tsx";

export default function Router() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
    </Routes>
  )
}
