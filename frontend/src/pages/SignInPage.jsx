import { SignIn } from "@clerk/react"

const SignInPage = () => {
  return (
    <div className="flex flex-col justify-center items-center">
      <h2 className="text-3xl text-[#0B1C30] text-center font-bold tracking-wider mb-8">Signin to APP Bit App</h2>
      <SignIn />
    </div>
  )
}

export default SignInPage
