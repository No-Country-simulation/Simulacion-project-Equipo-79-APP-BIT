import { SignUp } from "@clerk/react"
import { Link } from 'react-router';

const SignUpPage = () => {
  return (
    <div className="min-h-screen flex">
      <div className="w-1/2 hidden lg:flex flex-col justify-center items-center p-12"
        style={{
          background: "linear-gradient(135deg, #006B5F, #005a50, #004940)"
        }}
      >
        <h1 className="text-6xl font-bold tracking-tighter mb-6 text-white">BiT Admin</h1>
        <p className="text-xl text-white mb-8 tracking-wider uppercase">ESG Matching Portal</p>

        <div className="text-center max-w-md">
          <p className="text-2xl font-semibold mb-4 text-white">Creá tu cuenta en minutos</p>
          <p className="text-lg text-white/90 mb-8">
            Registrá tu empresa y empezá a encontrar el talento que cumple con tus objetivos ESG.
          </p>
        </div>

        <div className="flex items-center gap-8 mt-12">
          <div className="flex items-center gap-2 text-white">
            <i className="fa-solid fa-building text-2xl" />
            <span className="font-medium">Empresa</span>
          </div>
          <div className="flex items-center gap-2 text-white">
            <i className="fa-solid fa-leaf text-2xl" />
            <span className="font-medium">ESG</span>
          </div>
          <div className="flex items-center gap-2 text-white">
            <i className="fa-solid fa-rocket text-2xl" />
            <span className="font-medium">Crecé</span>
          </div>
        </div>
      </div>

      <div className="w-full lg:w-1/2 bg-[#F8F9FF] flex items-center justify-center p-8 md:p-12">
        <div className="w-full max-w-md">
          <div className="mb-10">
            <h2 className="text-4xl font-semibold mb-1 text-[#0B1C30]">Crear cuenta</h2>
            <p className="text-[#6B7280]">Registrá tu empresa para empezar</p>
          </div>

          <SignUp />

          <p className="text-sm text-[#6B7280] text-center mt-6">
            ¿Ya tenés cuenta?{' '}
            <Link to="/sign-in" className="text-[#006B5F] hover:underline font-semibold">
              Iniciá sesión
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

export default SignUpPage
