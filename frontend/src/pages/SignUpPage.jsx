import { SignUp } from "@clerk/react"

const SignUpPage = () => {
  return (
    <div className="flex flex-col justify-center items-center">
      <h2 className="text-3xl text-[#0B1C30] text-center font-bold tracking-wider mb-8">Signup to APP Bit App</h2>
      <SignUp />
    </div>
  )
}

export default SignUpPage
