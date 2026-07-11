import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly errorMessage = signal<string | null>(null);
  readonly loading = signal(false);

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  constructor() {
    const demoEmail = this.route.snapshot.queryParamMap.get('email');
    if (demoEmail) {
      this.form.patchValue({ email: demoEmail, password: 'Demo1234!' });
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService
      .login({
        email: this.form.value.email!,
        password: this.form.value.password!,
      })
      .subscribe({
        next: (response) => {
          this.loading.set(false);
          // A un socio lo llevamos directo a la pagina de reservas de su
          // negocio; el resto de roles pasan por el panel general.
          const target = response.user.role === 'MEMBER' ? '/schedule' : '/dashboard';
          this.router.navigate([target]);
        },
        error: (err: HttpErrorResponse) => {
          this.loading.set(false);
          this.errorMessage.set(this.resolveErrorMessage(err));
        },
      });
  }

  private resolveErrorMessage(err: HttpErrorResponse): string {
    const code = err.error?.error;
    if (code === 'ACCOUNT_PENDING' || code === 'ACCOUNT_REJECTED') {
      return err.error.message;
    }
    return 'Email o contraseña incorrectos.';
  }
}
