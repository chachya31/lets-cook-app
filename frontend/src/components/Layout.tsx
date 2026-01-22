import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { useAuthStore } from '../features/auth/stores/useAuthStore'
import { Button } from '@/components/ui/button'

interface LayoutProps {
  children: ReactNode
}

export const Layout = ({ children }: LayoutProps) => {
  const { isAuthenticated, user, logout } = useAuthStore()

  const displayName = user?.displayName || user?.nickname || user?.email

  return (
    <div className="min-h-screen bg-background">
      <nav className="border-b bg-card">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <div className="flex h-14 items-center justify-between sm:h-16">
            <Link to="/" className="text-lg font-bold text-foreground sm:text-xl">
              Let&apos;s Cook
            </Link>
            <div className="flex items-center gap-2 sm:gap-4">
              {isAuthenticated ? (
                <>
                  <span className="hidden text-sm text-muted-foreground sm:inline">
                    Welcome, {displayName}
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
      <main className="mx-auto max-w-7xl px-4 py-6 sm:px-6 sm:py-8 lg:px-8">
        {children}
      </main>
    </div>
  )
}
