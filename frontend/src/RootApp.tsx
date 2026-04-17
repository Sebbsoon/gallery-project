import {
  ClerkProvider,
  SignedIn,
  SignedOut,
  SignInButton,
  UserButton,
  useAuth,
  useUser,
} from '@clerk/clerk-react'
import App from './App'

const clerkPublishableKey = import.meta.env.VITE_CLERK_PUBLISHABLE_KEY

function AppWithClerk() {
  const { isSignedIn, getToken } = useAuth()
  const { user } = useUser()

  const userLabel = user?.primaryEmailAddress?.emailAddress ?? user?.fullName ?? user?.id ?? 'user'

  return (
    <>
      <div className="auth-controls">
        <SignedOut>
          <SignInButton mode="modal">
            <button type="button">Sign in</button>
          </SignInButton>
        </SignedOut>
        <SignedIn>
          <UserButton />
        </SignedIn>
      </div>
      <App auth={{ isSignedIn: Boolean(isSignedIn), getToken, userLabel }} />
    </>
  )
}

function AppWithoutClerk() {
  return (
    <>
      <div className="auth-controls">
        <span>
          Clerk is disabled. Set <code>VITE_CLERK_PUBLISHABLE_KEY</code> to enable sign-in.
        </span>
      </div>
      <App auth={{ isSignedIn: false }} />
    </>
  )
}

export default function RootApp() {
  if (!clerkPublishableKey) {
    return <AppWithoutClerk />
  }

  return (
    <ClerkProvider publishableKey={clerkPublishableKey}>
      <AppWithClerk />
    </ClerkProvider>
  )
}
