import { Route, Routes} from "react-router-dom";
import App from "@/app/App.tsx";

export default function Router() {
  return (
    <Routes>
      <Route path="/" element={<App />} />
    </Routes>
  )
}