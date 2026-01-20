import { Link } from 'react-router-dom';
import { Layout } from '@/components/Layout';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { useAuthStore } from '../../auth/stores/useAuthStore';

export const LandingPage = () => {
  const { isAuthenticated, user } = useAuthStore();

  return (
    <Layout>
      <div className="flex flex-col items-center">
        {/* Hero Section */}
        <section className="w-full py-8 sm:py-12 md:py-16 text-center">
          <h1 className="text-3xl sm:text-4xl md:text-5xl font-bold text-foreground mb-4">
            Welcome to Cooking App
          </h1>
          <p className="text-lg sm:text-xl text-muted-foreground mb-8 max-w-2xl mx-auto px-4">
            Your personal cooking assistant for delicious, healthy meals
          </p>

          {!isAuthenticated && (
            <Button asChild size="lg" className="text-base sm:text-lg px-6 sm:px-8">
              <Link to="/login">Get Started</Link>
            </Button>
          )}
        </section>

        <Separator className="my-4 sm:my-8 max-w-4xl w-full" />

        {/* Features / Status Section */}
        <section className="w-full max-w-4xl px-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6">
            {/* Auth Status Card */}
            <Card>
              <CardHeader>
                <CardTitle className="text-lg sm:text-xl">Auth Status</CardTitle>
                <CardDescription>Current authentication state</CardDescription>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="flex justify-between items-center">
                  <span className="text-sm text-muted-foreground">Status:</span>
                  <span
                    className={
                      isAuthenticated
                        ? 'text-primary font-medium'
                        : 'text-destructive font-medium'
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
                        <span className="font-mono text-xs">{user.id}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-muted-foreground">Name:</span>
                        <span>{user.name}</span>
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
                    <span className="text-primary">✓</span>
                    Browse healthy recipes
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="text-primary">✓</span>
                    Save your favorites
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="text-primary">✓</span>
                    Create shopping lists
                  </li>
                  <li className="flex items-center gap-2">
                    <span className="text-primary">✓</span>
                    Track nutritional info
                  </li>
                </ul>
              </CardContent>
            </Card>
          </div>
        </section>
      </div>
    </Layout>
  );
};
