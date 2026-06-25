// ? assets
import DashboardIcon from "../components/icons/DashboardIcon"
import SignOutIcon from "../components/icons/SignOutIcon"
import SupportIcon from "../components/icons/SupportIcon"
import JobsIcon from "../components/icons/JobsIcon"
import InsightsIcon from "../components/icons/InsightsIcon"
import SettingsIcon from "../components/icons/SettingsIcon"
import NotificationIcon from "../components/icons/NotificationIcon"
import HamburgerMenuIcon from "../components/icons/HamburgerMenuIcon"
import SearchIcon from '../components/icons/SearchIcon'
// ? react deps
import { useState } from "react"
import { useLocation, useSearchParams, Link } from "react-router"
import { Show, SignInButton, SignOutButton, SignUpButton, UserButton } from "@clerk/react"

const routeConfig = {
  '/': {
    placeholder: 'Search…'
  },
  '/job': {
    placeholder: 'Search Jobs…'
  },
  '/insights': {
    placeholder: 'Search insights…'
  }
}

const Layout = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { pathname } = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const query = searchParams.get('q') ?? '';

  const { placeholder } = routeConfig[pathname] ?? {
    title: 'Home',
    placeholder: 'Search…'
  };

  const handleSearchChange = (e) => {
    const value = e.target.value;

    if (value) {
      setSearchParams({ q: value });
    } else {
      searchParams.delete('q');
      setSearchParams(searchParams);
    }
  };

  return (
    <>
      <div className="flex">
        {/* // ? sidebar */}
        <aside className={`grid min-h-dvh bg-[#EFF4FF]/20 backdrop-blur-sm border border-[#EFF4FF]/20 shadow-lg grid-rows-[auto_1fr_auto] py-2 px-6 w-65 text-[#45464D] z-50 ${!sidebarOpen ? '-translate-x-65' : 'x-translate-0'} fixed md:static md:translate-x-0 transition-transform duration-300 ease-in-out `}>
          {/* //* logo */}
          <div id="aside-logo" className="mx-4">
            <a href="/">
              <p className="xl:text-[28px] md:text-[24px] text-[22px] font-extrabold">BiT Admin</p>
              <span className="font-medium text-[12px]">ESG  Matching Portal</span>
            </a>
          </div>
          {/* //* nav */}
          <ul className="[&_a]:inline-flex [&_a]:gap-2.5 [&_a]:items-center [&_a]:mx-4 [&_li]:my-2.5 [&_li]:py-2 mt-8 [&>li]:hover:bg-[#6DF5E1] [&>.activebtn]:bg-[#6DF5E1] *:rounded-lg *:cursor-pointer *:hover:text-black *:hover:transition-colors">
            <Show when="signed-in">
              <li className={pathname === '/' ? 'activebtn' : ''}><Link to="/"><DashboardIcon />Dashboard</Link></li>
              <li className={pathname.startsWith('/job') ? 'activebtn' : ''}><Link to="/job"><JobsIcon />Jobs</Link></li>
              <li className={pathname === '/insights' ? 'activebtn' : ''}><Link to="/insights"><InsightsIcon />Insights</Link></li>
              <li className={pathname === '/settings' ? 'activebtn' : ''}><Link to="/settings"><SettingsIcon />Settings</Link></li>
            </Show>
          </ul>
          {/* //* footer */}
          <div id="aside-footer" className="flex flex-col gap-5  *:hover:transition-colors [&_a]:mx-4">
            <Show when="signed-in">
              <Link to="/create-job" className="bg-[#006B5F] text-[#FFFFFF] rounded-xl px-4 py-2 cursor-pointer hover:brightness-95 transition-colors text-center hover:text-white">Post New Job</Link>
              <Link to="/support" className="inline-flex gap-4 items-center"><SupportIcon />Support</Link>
              <Link to="/settings" className="inline-flex gap-4 items-center cursor-pointer"><SignOutIcon /><SignOutButton className="cursor-pointer" /></Link>
              {/* <SignOutButton className="bg-[#006B5F] text-[#FFFFFF] rounded-xl px-4 py-2 cursor-pointer hover:brightness-95 transition-colors text-center" /> */}
            </Show>
            <Show when="signed-out">
              <SignInButton className="bg-[#006B5F] text-[#FFFFFF] rounded-xl px-4 py-2 cursor-pointer hover:brightness-95 transition-colors text-center" />
            </Show>
          </div>
        </aside>
        {/* // ? header */}
        <div className="flex flex-col justify-between w-full">
          <header className="flex justify-between items-center py-6 bg-[#F8F9FF] min-h-16">
            <div className="bg-[#E5EEFF] rounded-lg flex justify-between items-center gap-5 px-2 md:px-4 ml-3 md:ml-6 py-4">
              <SearchIcon />
              <input type="search" name="searchbar" id="searchbar" placeholder={placeholder} value={query} onChange={handleSearchChange} className="text-[#6B7280] text-[12px] md:text-[14px] outline-none" />
            </div>
            {/* //* items dekstop */}
            <div className="hidden md:flex justify-between items-center gap-4 md:pr-6">
              <div>
                <Show when="signed-in">
                  <div className="flex gap-4 items-center">
                    <NotificationIcon className="cursor-pointer" />
                    <UserButton />
                  </div>
                </Show>
                <Show when="signed-out">
                  <SignUpButton className="border-2 border-[#006B5F] text-[#006B5F] hover:bg-[#006B5F] hover:text-white rounded-xl px-4 py-2 cursor-pointer hover:brightness-95 transition-colors text-center mr-3" />
                  <SignInButton className="border-2 border-transparent bg-[#006B5F] text-[#FFFFFF] rounded-xl px-4 py-2 cursor-pointer hover:brightness-95 transition-colors text-center" />
                </Show>
              </div>
              {/* <img className="inline-block size-8 rounded-full cursor-pointer hover:scale-105 transition-transform" src="https://images.unsplash.com/photo-1568602471122-7832951cc4c5?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=facearea&facepad=2&w=300&h=300&q=80" alt="Avatar" /> */}
            </div>
            {/* //* items mobile */}
            <div className="px-2 md:px-6 py-2 flex items-center gap-6 md:gap-8 md:hidden mr-3 md:mr-6">
              {/* <button className="cursor-pointer" onClick={() => setSidebarOpen(!sidebarOpen)} type="button">
                <NotificationIcon className="cursor-pointer" />
              </button> */}
              <UserButton />
              {/* <img className="inline-block size-8 rounded-full md:hidden cursor-pointer hover:scale-105 transition-transform" src="https://images.unsplash.com/photo-1568602471122-7832951cc4c5?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=facearea&facepad=2&w=300&h=300&q=80" alt="Avatar" /> */}
              <button className="cursor-pointer" onClick={() => setSidebarOpen(!sidebarOpen)} type="button">
                <HamburgerMenuIcon />
              </button>
            </div>
          </header>
          <div className="w-full h-fit px-7.25 bg-[#F8F9FF]">
            {children}
          </div>
        </div>
      </div >
    </>
  )
}

export default Layout
