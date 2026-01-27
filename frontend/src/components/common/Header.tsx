import {
  Bot,
  Calendar,
  LogOut,
  Menu,
  Package,
  Plus,
  Search,
  Shield,
  ShoppingCart,
  User,
} from 'lucide-react';
import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { Avatar, AvatarFallback, AvatarImage } from '../ui/avatar';
import { Button } from '../ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '../ui/dropdown-menu';
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from '../ui/sheet';

interface NavItem {
  label: string;
  href: string;
  icon: React.ReactNode;
  authRequired?: boolean;
  adminOnly?: boolean;
}

/**
 * ヘッダーコンポーネント
 * アプリケーション全体のナビゲーションバー
 */
const Header: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const currentUser = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const isLoggedIn = !!currentUser;
  const isAdmin = currentUser?.roles?.includes('Admins') ?? false;
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const navItems: NavItem[] = [
    { label: t('recipe.search.title'), href: '/recipes', icon: <Search className="h-4 w-4" /> },
    { label: t('schedule.title'), href: '/schedules', icon: <Calendar className="h-4 w-4" /> },
    {
      label: t('shoppingList.title'),
      href: '/shopping-list',
      icon: <ShoppingCart className="h-4 w-4" />,
    },
    {
      label: t('inventory.title'),
      href: '/inventory',
      icon: <Package className="h-4 w-4" />,
      authRequired: true,
    },
    {
      label: 'AI Chat',
      href: '/chat',
      icon: <Bot className="h-4 w-4" />,
      authRequired: true,
    },
    {
      label: t('admin.dashboard.title'),
      href: '/admin',
      icon: <Shield className="h-4 w-4" />,
      adminOnly: true,
    },
  ];

  const getDisplayName = () => {
    return currentUser?.displayName || currentUser?.nickname || currentUser?.email || 'ユーザー';
  };

  const getInitials = () => {
    const name = currentUser?.displayName || currentUser?.nickname || currentUser?.email || '';
    if (!name) return 'U';
    return name.charAt(0).toUpperCase();
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
    setMobileMenuOpen(false);
  };

  const handleCreateRecipe = () => {
    navigate('/recipes/new');
    setMobileMenuOpen(false);
  };

  const filteredNavItems = navItems.filter((item) => {
    if (item.adminOnly) return isAdmin;
    if (item.authRequired) return isLoggedIn;
    return true;
  });

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
  );

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2">
          <img src="/logo.svg" alt="Let's Cook" className="h-12 w-auto" />
          <span className="text-xl font-bold text-primary">{t('app.title')}</span>
        </Link>

        {/* Desktop Navigation */}
        <nav className="hidden items-center gap-6 lg:flex">
          <NavLinks />
        </nav>

        {/* Desktop Actions */}
        <div className="hidden items-center gap-3 lg:flex">
          {isLoggedIn ? (
            <>
              <Button onClick={handleCreateRecipe} size="sm" className="gap-1.5">
                <Plus className="h-4 w-4" />
                {t('recipe.create.button')}
              </Button>
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" className="relative h-10 w-10 rounded-full">
                    <Avatar className="h-10 w-10">
                      <AvatarImage
                        src={currentUser?.profileImageUrl || undefined}
                        alt={getDisplayName()}
                      />
                      <AvatarFallback className="bg-orange-500 text-white">
                        {getInitials()}
                      </AvatarFallback>
                    </Avatar>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent className="w-56" align="end" forceMount>
                  <DropdownMenuLabel className="font-normal">
                    <div className="flex flex-col space-y-1">
                      <p className="text-sm font-medium leading-none">{getDisplayName()}</p>
                      <p className="text-xs leading-none text-muted-foreground">
                        {currentUser?.email}
                      </p>
                    </div>
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={() => navigate('/profile/edit')}>
                    <User className="mr-2 h-4 w-4" />
                    {t('profile.title')}
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={handleLogout}>
                    <LogOut className="mr-2 h-4 w-4" />
                    {t('auth.logout')}
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </>
          ) : (
            <>
              <Button asChild variant="ghost" size="sm">
                <Link to="/login">{t('auth.login')}</Link>
              </Button>
              <Button asChild variant="outline" size="sm">
                <Link to="/register">{t('auth.register')}</Link>
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
                {t('app.title')}
              </SheetTitle>
            </SheetHeader>
            <div className="mt-6 flex flex-col gap-4">
              {/* Mobile Navigation */}
              <nav className="flex flex-col gap-1">
                <NavLinks mobile />
              </nav>

              <div className="my-2 h-px bg-border" />

              {/* Mobile Actions */}
              {isLoggedIn ? (
                <>
                  <div className="flex items-center gap-3 px-3">
                    <Avatar className="h-10 w-10">
                      <AvatarImage
                        src={currentUser?.profileImageUrl || undefined}
                        alt={getDisplayName()}
                      />
                      <AvatarFallback className="bg-orange-500 text-white">
                        {getInitials()}
                      </AvatarFallback>
                    </Avatar>
                    <div className="flex flex-col">
                      <span className="text-sm font-medium">{getDisplayName()}</span>
                      <span className="text-xs text-muted-foreground">{currentUser?.email}</span>
                    </div>
                  </div>
                  <Button onClick={handleCreateRecipe} className="gap-1.5">
                    <Plus className="h-4 w-4" />
                    {t('recipe.create.button')}
                  </Button>
                  <Button
                    variant="ghost"
                    className="justify-start gap-2"
                    onClick={() => {
                      navigate('/profile/edit');
                      setMobileMenuOpen(false);
                    }}
                  >
                    <User className="h-4 w-4" />
                    {t('profile.title')}
                  </Button>
                  <Button
                    variant="ghost"
                    className="justify-start gap-2 text-destructive"
                    onClick={handleLogout}
                  >
                    <LogOut className="h-4 w-4" />
                    {t('auth.logout')}
                  </Button>
                </>
              ) : (
                <>
                  <Button asChild className="w-full">
                    <Link to="/login" onClick={() => setMobileMenuOpen(false)}>
                      {t('auth.login')}
                    </Link>
                  </Button>
                  <Button asChild variant="outline" className="w-full">
                    <Link to="/register" onClick={() => setMobileMenuOpen(false)}>
                      {t('auth.register')}
                    </Link>
                  </Button>
                </>
              )}
            </div>
          </SheetContent>
        </Sheet>
      </div>
    </header>
  );
};

export default Header;
