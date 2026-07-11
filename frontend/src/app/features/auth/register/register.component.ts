import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { Role } from '../../../core/auth/auth.models';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly roles: Role[] = ['SUPER_ADMIN', 'BUSINESS_ADMIN', 'STAFF', 'MEMBER'];
  readonly errorMessage = signal<string | null>(null);
  readonly loading = signal(false);

  // Cuando llega desde "Mi negocio" -> "Dar de alta staff", o desde una tarjeta
  // de disciplina en la landing, el rol y el negocio vienen fijados por query
  // params y no deben poder cambiarse desde el formulario.
  readonly lockedFields = signal(false);

  // true si el registro bloqueado es el de un socio uniendose a un negocio
  // (en vez de un alta de staff hecha por su administrador).
  readonly isMemberJoin = signal(false);

  // Se marca a true cuando el registro de un socio queda pendiente de
  // aprobacion: no hay sesion que iniciar todavia, solo mostramos confirmacion.
  readonly pendingApproval = signal(false);

  readonly form = this.fb.group({
    fullName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role: ['SUPER_ADMIN' as Role, [Validators.required]],
    businessId: [''],
  });

  constructor() {
    const queryParams = this.route.snapshot.queryParamMap;
    const presetRole = queryParams.get('role') as Role | null;
    const presetBusinessId = queryParams.get('businessId');

    if (presetRole && presetBusinessId) {
      this.lockedFields.set(true);
      this.isMemberJoin.set(presetRole === 'MEMBER');
      this.form.patchValue({ role: presetRole, businessId: presetBusinessId });
      this.form.get('role')?.disable();
      this.form.get('businessId')?.disable();
    }
  }

  requiresBusinessId(): boolean {
    return this.form.getRawValue().role !== 'SUPER_ADMIN';
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const value = this.form.getRawValue();

    this.authService
      .register({
        fullName: value.fullName!,
        email: value.email!,
        password: value.password!,
        role: value.role!,
        businessId: value.businessId || null,
      })
      .subscribe({
        next: (response) => {
          this.loading.set(false);
          if (!response.accessToken) {
            this.pendingApproval.set(true);
            return;
          }
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          this.loading.set(false);
          this.errorMessage.set(err.error?.message ?? 'No se pudo completar el registro.');
        },
      });
  }
}
