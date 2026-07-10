import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
  },
  {
    path: 'businesses',
    canActivate: [authGuard, roleGuard(['SUPER_ADMIN'])],
    loadComponent: () =>
      import('./features/business/businesses-list/businesses-list.component').then(
        (m) => m.BusinessesListComponent,
      ),
  },
  {
    path: 'my-business',
    canActivate: [authGuard, roleGuard(['BUSINESS_ADMIN'])],
    loadComponent: () =>
      import('./features/business/my-business/my-business.component').then((m) => m.MyBusinessComponent),
  },
  {
    path: 'activities',
    canActivate: [authGuard, roleGuard(['BUSINESS_ADMIN', 'STAFF'])],
    loadComponent: () =>
      import('./features/booking/activities/activities.component').then((m) => m.ActivitiesComponent),
  },
  {
    path: 'schedule',
    canActivate: [authGuard, roleGuard(['MEMBER'])],
    loadComponent: () =>
      import('./features/booking/schedule/schedule.component').then((m) => m.ScheduleComponent),
  },
  { path: '**', redirectTo: 'login' },
];
