import { Outlet } from "react-router-dom";
import Navbar from "./NavbarAdmin";

export default function Layout() {
  return (
    <>
      <Navbar />
      <main style={{ padding: "2rem" }}>
        <Outlet />
      </main>
    </>
  );
}
