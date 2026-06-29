import { Link } from 'react-router';

const Settings = () => {
  return (
    <div className="p-6 max-w-4xl mx-auto space-y-6">
      <h1 className="text-2xl font-bold text-gray-800">Settings</h1>
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
        <h2 className="text-sm font-bold text-gray-700 mb-4 uppercase tracking-wider">Company</h2>
        <Link to="/register-company"
          className="inline-flex items-center gap-2 bg-[#006B5F] hover:bg-[#005a50] text-white text-sm font-semibold px-5 py-2.5 rounded-lg transition-all shadow-sm cursor-pointer">
          Register Company
        </Link>
      </div>
    </div>
  )
}

export default Settings
