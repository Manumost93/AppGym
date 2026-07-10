import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BusinessService } from '../../../core/business/business.service';
import { Business, BusinessType } from '../../../core/business/business.models';

@Component({
  selector: 'app-businesses-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './businesses-list.component.html',
})
export class BusinessesListComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly businessService = inject(BusinessService);

  readonly businesses = signal<Business[]>([]);
  readonly loading = signal(true);
  readonly creating = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly types: BusinessType[] = ['GYM', 'CROSSFIT_BOX', 'PADEL_CLUB'];

  readonly form = this.fb.group({
    name: ['', [Validators.required]],
    type: ['GYM' as BusinessType, [Validators.required]],
    description: [''],
    contactEmail: [''],
    contactPhone: [''],
    address: [''],
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.businessService.listAll().subscribe({
      next: (businesses) => {
        this.businesses.set(businesses);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.creating.set(true);
    this.errorMessage.set(null);

    this.businessService
      .create({
        name: this.form.value.name!,
        type: this.form.value.type!,
        description: this.form.value.description || undefined,
        contactEmail: this.form.value.contactEmail || undefined,
        contactPhone: this.form.value.contactPhone || undefined,
        address: this.form.value.address || undefined,
      })
      .subscribe({
        next: () => {
          this.creating.set(false);
          this.form.reset({ type: 'GYM' });
          this.reload();
        },
        error: () => {
          this.creating.set(false);
          this.errorMessage.set('No se pudo crear el negocio.');
        },
      });
  }
}
