import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { BusinessService } from '../../../core/business/business.service';
import { Business, MembershipPlan } from '../../../core/business/business.models';
import { AiService } from '../../../core/ai/ai.service';
import { InsightsResponse } from '../../../core/ai/ai.models';

@Component({
  selector: 'app-my-business',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './my-business.component.html',
})
export class MyBusinessComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly businessService = inject(BusinessService);
  private readonly authService = inject(AuthService);
  private readonly aiService = inject(AiService);

  readonly business = signal<Business | null>(null);
  readonly plans = signal<MembershipPlan[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly editingPlanId = signal<string | null>(null);

  readonly insights = signal<InsightsResponse | null>(null);
  readonly loadingInsights = signal(false);

  readonly businessId = this.authService.currentUser()?.businessId ?? null;

  readonly form = this.fb.group({
    name: ['', [Validators.required]],
    description: [''],
    priceCents: [0, [Validators.required, Validators.min(0)]],
    currency: ['EUR', [Validators.required]],
    durationDays: [30, [Validators.required, Validators.min(1)]],
  });

  ngOnInit(): void {
    this.businessService.getMyBusiness().subscribe((business) => this.business.set(business));
    this.reloadPlans();
    this.loadInsights();
  }

  loadInsights(): void {
    this.loadingInsights.set(true);
    this.aiService.insights().subscribe({
      next: (insights) => {
        this.insights.set(insights);
        this.loadingInsights.set(false);
      },
      error: () => this.loadingInsights.set(false),
    });
  }

  reloadPlans(): void {
    this.loading.set(true);
    this.businessService.listPlans().subscribe({
      next: (plans) => {
        this.plans.set(plans);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  edit(plan: MembershipPlan): void {
    this.editingPlanId.set(plan.id);
    this.form.setValue({
      name: plan.name,
      description: plan.description ?? '',
      priceCents: plan.priceCents,
      currency: plan.currency,
      durationDays: plan.durationDays,
    });
  }

  cancelEdit(): void {
    this.editingPlanId.set(null);
    this.form.reset({ priceCents: 0, currency: 'EUR', durationDays: 30 });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set(null);

    const request = {
      name: this.form.value.name!,
      description: this.form.value.description || undefined,
      priceCents: this.form.value.priceCents!,
      currency: this.form.value.currency!,
      durationDays: this.form.value.durationDays!,
    };

    const editingId = this.editingPlanId();
    const request$ = editingId
      ? this.businessService.updatePlan(editingId, request)
      : this.businessService.createPlan(request);

    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.cancelEdit();
        this.reloadPlans();
      },
      error: () => {
        this.saving.set(false);
        this.errorMessage.set('No se pudo guardar el plan.');
      },
    });
  }

  deletePlan(plan: MembershipPlan): void {
    this.businessService.deletePlan(plan.id).subscribe(() => this.reloadPlans());
  }

  priceLabel(plan: MembershipPlan): string {
    return `${(plan.priceCents / 100).toFixed(2)} ${plan.currency}`;
  }
}
