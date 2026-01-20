import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { useAuthStore } from '../features/auth/stores/useAuthStore';
import { Button } from '@/components/ui/button';

interface LayoutProps {
  children: ReactNode;
}

export const Layout = ({ children }: LayoutProps) => {
  const { isAuthenticated, user, logout } = useAuthStore();

  return (
    <div className="min-h-screen bg-background">
      <nav className="border-b bg-card">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-14 sm:h-16 items-center">
            <Link to="/" className="text-lg sm:text-xl font-bold text-foreground">
              Cooking App
            </Link>
            <div className="flex items-center gap-2 sm:gap-4">
              {isAuthenticated ? (
                <>
                  <span className="hidden sm:inline text-sm text-muted-foreground">
                    Welcome, {user?.name}
                  </span>
                  <Button variant="secondary" size="sm" onClick={logout}>
                    Logout
                  </Button>
                </>
              ) : (
                <Button asChild size="sm">
                  <Link to="/login">Login</Link>
                </Button>
              )}
            </div>
          </div>
        </div>
      </nav>
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8">
        {children}
      </main>
    </div>
  );
};
