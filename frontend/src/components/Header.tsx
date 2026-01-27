import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  Search,
  Calendar,
  ShoppingCart,
  Package,
  MessageSquare,
  Settings,
  Plus,
  Menu,
  LogOut,
  User,
} from 'lucide-react'
import { useAuthStore } from '@/features/auth/stores/useAuthStore'
import { Button } from '@/components/ui/button'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from '@/components/ui/sheet'

interface NavItem {
  label: string
  href: string
  icon: React.ReactNode
  authRequired?: boolean
  adminOnly?: boolean
}

const navItems: NavItem[] = [
  { label: 'レシピ検索', href: '/recipes', icon: <Search className="h-4 w-4" /> },
  { label: 'スケジュール', href: '/schedule', icon: <Calendar className="h-4 w-4" /> },
  { label: '買い物リスト', href: '/shopping', icon: <ShoppingCart className="h-4 w-4" /> },
  {
    label: '在庫管理',
    href: '/inventory',
    icon: <Package className="h-4 w-4" />,
    authRequired: true,
  },
  {
    label: 'AIチャット',
    href: '/chat',
    icon: <MessageSquare className="h-4 w-4" />,
    authRequired: true,
  },
  { label: '管理画面', href: '/admin', icon: <Settings className="h-4 w-4" />, adminOnly: true },
]

export const Header = () => {
  const navigate = useNavigate()
  const { isAuthenticated, user, logout } = useAuthStore()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  const isAdmin = user?.roles?.includes('Admins') ?? false

  const getDisplayName = () => {
    return user?.displayName || user?.nickname || user?.email || 'ユーザー'
  }

  const getInitials = () => {
    const name = user?.displayName || user?.nickname || user?.email || ''
    if (!name) return 'U'
    return name.charAt(0).toUpperCase()
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
    setMobileMenuOpen(false)
  }

  const handleCreateRecipe = () => {
    navigate('/recipes/new')
    setMobileMenuOpen(false)
  }

  const handleRegister = () => {
    console.log('新規登録ボタンがクリックされました')
  }

  const filteredNavItems = navItems.filter((item) => {
    if (item.adminOnly) return isAdmin
    if (item.authRequired) return isAuthenticated
    return true
  })

  const NavLinks = ({ mobile = false }: { mobile?: boolean }) => (
    <>
      {filteredNavItems.map((item) => (
        <Link
          key={item.href}
          to={item.href}
          className={
            mobile
              ? 'flex items-center gap-3 rounded-lg px-3 py-2 text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground'
              : 'flex items-center gap-1.5 text-sm font-medium text-muted-foreground transition-colors hover:text-primary'
          }
          onClick={() => setMobileMenuOpen(false)}
        >
          {item.icon}
          {item.label}
        </Link>
      ))}
    </>
  )

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2">
          <img src="/logo.svg" alt="Let's Cook" className="h-8 w-8" />
          <span className="text-xl font-bold text-primary">Let&apos;s Cook</span>
        </Link>

        {/* Desktop Navigation */}
        <nav className="hidden items-center gap-6 lg:flex">
          <NavLinks />
        </nav>

        {/* Desktop Actions */}
        <div className="hidden items-center gap-3 lg:flex">
          {isAuthenticated ? (
            <>
              <Button onClick={handleCreateRecipe} size="sm" className="gap-1.5">
                <Plus className="h-4 w-4" />
                レシピ作成
              </Button>
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" className="relative h-9 w-9 rounded-full">
                    <Avatar className="h-9 w-9">
                      <AvatarImage
                        src={user?.profileImageUrl || undefined}
                        alt={getDisplayName()}
                      />
                      <AvatarFallback className="bg-primary text-primary-foreground">
                        {getInitials()}
                      </AvatarFallback>
                    </Avatar>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent className="w-56" align="end" forceMount>
                  <DropdownMenuLabel className="font-normal">
                    <div className="flex flex-col space-y-1">
                      <p className="text-sm font-medium leading-none">{getDisplayName()}</p>
                      <p className="text-xs leading-none text-muted-foreground">{user?.email}</p>
                    </div>
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={() => console.log('プロフィール編集')}>
                    <User className="mr-2 h-4 w-4" />
                    プロフィール編集
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={handleLogout}>
                    <LogOut className="mr-2 h-4 w-4" />
                    ログアウト
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </>
          ) : (
            <>
              <Button asChild variant="ghost" size="sm">
                <Link to="/login">ログイン</Link>
              </Button>
              <Button variant="outline" size="sm" onClick={handleRegister}>
                新規登録
              </Button>
            </>
          )}
        </div>

        {/* Mobile Menu Button */}
        <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
          <SheetTrigger asChild className="lg:hidden">
            <Button variant="ghost" size="icon">
              <Menu className="h-5 w-5" />
              <span className="sr-only">メニューを開く</span>
            </Button>
          </SheetTrigger>
          <SheetContent side="right" className="w-72">
            <SheetHeader>
              <SheetTitle className="flex items-center gap-2">
                <img src="/logo.svg" alt="Let's Cook" className="h-6 w-6" />
                Let&apos;s Cook
              </SheetTitle>
            </SheetHeader>
            <div className="mt-6 flex flex-col gap-4">
              {/* Mobile Navigation */}
              <nav className="flex flex-col gap-1">
                <NavLinks mobile />
              </nav>

              <div className="my-2 h-px bg-border" />

              {/* Mobile Actions */}
              {isAuthenticated ? (
                <>
                  <div className="flex items-center gap-3 px-3">
                    <Avatar className="h-10 w-10">
                      <AvatarImage
                        src={user?.profileImageUrl || undefined}
                        alt={getDisplayName()}
                      />
                      <AvatarFallback className="bg-primary text-primary-foreground">
                        {getInitials()}
                      </AvatarFallback>
                    </Avatar>
                    <div className="flex flex-col">
                      <span className="text-sm font-medium">{getDisplayName()}</span>
                      <span className="text-xs text-muted-foreground">{user?.email}</span>
                    </div>
                  </div>
                  <Button onClick={handleCreateRecipe} className="gap-1.5">
                    <Plus className="h-4 w-4" />
                    レシピ作成
                  </Button>
                  <Button
                    variant="ghost"
                    className="justify-start gap-2"
                    onClick={() => console.log('プロフィール編集')}
                  >
                    <User className="h-4 w-4" />
                    プロフィール編集
                  </Button>
                  <Button
                    variant="ghost"
                    className="justify-start gap-2 text-destructive"
                    onClick={handleLogout}
                  >
                    <LogOut className="h-4 w-4" />
                    ログアウト
                  </Button>
                </>
              ) : (
                <>
                  <Button asChild className="w-full">
                    <Link to="/login" onClick={() => setMobileMenuOpen(false)}>
                      ログイン
                    </Link>
                  </Button>
                  <Button variant="outline" className="w-full" onClick={handleRegister}>
                    新規登録
                  </Button>
                </>
              )}
            </div>
          </SheetContent>
        </Sheet>
      </div>
    </header>
  )
}
