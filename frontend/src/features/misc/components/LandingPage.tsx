import { Link } from 'react-router-dom'
import { Layout } from '@/components/Layout'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Separator } from '@/components/ui/separator'
import { useAuthStore } from '../../auth/stores/useAuthStore'

export const LandingPage = () => {
  const { isAuthenticated, user } = useAuthStore()

  const displayName = user?.displayName || user?.nickname || user?.email

  return (
    <Layout>
      <div className="flex flex-col items-center">
        {/* Hero Section */}
        <section className="w-full py-8 text-center sm:py-12 md:py-16">
          <h1 className="mb-4 text-3xl font-bold text-foreground sm:text-4xl md:text-5xl">
            Welcome to Let&apos;s Cook
          </h1>
          <p className="mx-auto mb-8 max-w-2xl px-4 text-lg text-muted-foreground sm:text-xl">
            Your personal cooking assistant for delicious, healthy meals
          </p>

          {!isAuthenticated && (
            <Button asChild size="lg" className="bg-emerald-600 px-6 text-base hover:bg-emerald-700 sm:px-8 sm:text-lg">
              <Link to="/login">Get Started</Link>
            </Button>
          )}
        </section>

        <Separator className="my-4 w-full max-w-4xl sm:my-8" />

        {/* Features / Status Section */}
        <section className="w-full max-w-4xl px-4">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 sm:gap-6">
            {/* Auth Status Card */}
            <Card>
              <CardHeader>
                <CardTitle className="text-lg sm:text-xl">Auth Status</CardTitle>
                <CardDescription>Current authentication state</CardDescription>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-sm text-muted-foreground">Status:</span>
                  <span
                    className={
                      isAuthenticated
                        ? 'font-medium text-emerald-600'
                        : 'font-medium text-destructive'
                    }
                  >
                    {isAuthenticated ? 'Authenticated' : 'Not logged in'}
                  </span>
                </div>
                {user && (
                  <>
                    <Separator />
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span className="text-muted-foreground">User ID:</span>
                        <span className="font-mono text-xs">{user.userId}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-muted-foreground">Name:</span>
                        <span>{displayName}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-muted-foreground">Email:</span>
                        <span className="text-xs sm:text-sm">{user.email}</span>
                      </div>
                    </div>
                  </>
                )}
              </CardContent>
            </Card>

            {/* Features Card */}
            <Card>
              <CardHeader>
                <CardTitle className="text-lg sm:text-xl">Features</CardTitle>
                <CardDescription>What you can do with this app</CardDescription>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2 text-sm text-muted-foreground">
                  <li className="flex items-center gap-2">
                    <span className="text-emerald-600">✓</span>
                    Browse healthy recipes
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="text-emerald-600">✓</span>
                    Save your favorites
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="text-emerald-600">✓</span>
                    Create shopping lists
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="text-emerald-600">✓</span>
                    Track nutritional info
                  </li>
                </ul>
              </CardContent>
            </Card>
          </div>
        </section>
      </div>
    </Layout>
  )
}
