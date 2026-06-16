import DashboardIcon from "../components/icons/DashboardIcon"
import SignOutIcon from "../components/icons/SignOutIcon"
import SupportIcon from "../components/icons/SupportIcon"
import JobsIcon from "../components/icons/JobsIcon"
import InsightsIcon from "../components/icons/InsightsIcon"
import SettingsIcon from "../components/icons/SettingsIcon"


const Layout = ({ children }) => {
  return (
    <div className="flex">
      <aside className="grid min-h-dvh bg-[#EFF4FF] grid-rows-[auto_1fr_auto] py-8 px-2 max-w-60 text-[#45464D]">
        <div id="aside-logo">
          <a href="/">
            <p className="text-[32px] font-extrabold">BiT Admin</p>
            <p className="font-medium">ESG  Matching Portal</p>
          </a>
        </div>
        <ul className="[&_a]:inline-flex [&_a]:gap-2.5 [&_a]:items-center [&_a]:mx-4 [&_li]:my-2.5 [&_li]:py-2 mt-8 [&>li]:hover:bg-[#6DF5E1] [&>.activebtn]:bg-[#6DF5E1] *:rounded-lg *:cursor-pointer *:hover:text-black *:hover:transition-colors">
          <li className="activebtn"><a href="#"><DashboardIcon />Dashboard</a></li>
          <li><a href="#"><JobsIcon />Jobs</a></li>
          <li><a href="#"><InsightsIcon />Insights</a></li>
          <li><a href="#"><SettingsIcon />Settings</a></li>
        </ul>

        <div id="aside-footer" className="flex flex-col gap-5 [&_a]:hover:text-black *:hover:transition-colors [&_a]:mx-4">
          <button className="bg-[#006B5F] text-[#FFFFFF] rounded-xl px-4 py-2 cursor-pointer hover:brightness-95 transition-colors">Post New Job</button>
          <a href="#" className="inline-flex gap-4 items-center"><SupportIcon />Support</a>
          <a href="#" className="inline-flex gap-4 items-center"><SignOutIcon />Sign Out</a>
        </div>
      </aside>
      <div className="text-center">{children}</div>
    </div>
  )
}

export default Layout
