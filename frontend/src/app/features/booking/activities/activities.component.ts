import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BookingService } from '../../../core/booking/booking.service';
import { Activity, ActivityType, ScheduleSlot } from '../../../core/booking/booking.models';

@Component({
  selector: 'app-activities',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './activities.component.html',
})
export class ActivitiesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly bookingService = inject(BookingService);

  readonly types: ActivityType[] = ['CLASS', 'COURT'];
  readonly activities = signal<Activity[]>([]);
  readonly slots = signal<ScheduleSlot[]>([]);
  readonly loading = signal(true);
  readonly creatingActivity = signal(false);
  readonly creatingSlotFor = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);

  readonly activityForm = this.fb.group({
    type: ['CLASS' as ActivityType, [Validators.required]],
    name: ['', [Validators.required]],
    description: [''],
    capacity: [10, [Validators.required, Validators.min(1)]],
    durationMinutes: [60, [Validators.required, Validators.min(1)]],
    instructorName: [''],
  });

  readonly slotForm = this.fb.group({
    startTime: ['', [Validators.required]],
  });

  ngOnInit(): void {
    this.reloadActivities();
    this.reloadSlots();
  }

  reloadActivities(): void {
    this.loading.set(true);
    this.bookingService.listActivities().subscribe({
      next: (activities) => {
        this.activities.set(activities);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  reloadSlots(): void {
    this.bookingService.listSlots().subscribe((slots) => this.slots.set(slots));
  }

  submitActivity(): void {
    if (this.activityForm.invalid) {
      this.activityForm.markAllAsTouched();
      return;
    }

    this.creatingActivity.set(true);
    this.errorMessage.set(null);

    this.bookingService
      .createActivity({
        type: this.activityForm.value.type!,
        name: this.activityForm.value.name!,
        description: this.activityForm.value.description || undefined,
        capacity: this.activityForm.value.capacity!,
        durationMinutes: this.activityForm.value.durationMinutes!,
        instructorName: this.activityForm.value.instructorName || undefined,
      })
      .subscribe({
        next: () => {
          this.creatingActivity.set(false);
          this.activityForm.reset({ type: 'CLASS', capacity: 10, durationMinutes: 60 });
          this.reloadActivities();
        },
        error: () => {
          this.creatingActivity.set(false);
          this.errorMessage.set('No se pudo crear la actividad.');
        },
      });
  }

  startSlotFor(activityId: string): void {
    this.creatingSlotFor.set(activityId);
    this.slotForm.reset();
  }

  submitSlot(activityId: string): void {
    if (this.slotForm.invalid) {
      this.slotForm.markAllAsTouched();
      return;
    }

    const startTime = new Date(this.slotForm.value.startTime!).toISOString();

    this.bookingService.createSlot({ activityId, startTime }).subscribe({
      next: () => {
        this.creatingSlotFor.set(null);
        this.reloadSlots();
      },
      error: () => this.errorMessage.set('No se pudo crear la franja horaria.'),
    });
  }

  deactivate(activity: Activity): void {
    this.bookingService.deactivateActivity(activity.id).subscribe(() => this.reloadActivities());
  }
}
