import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BookingService } from '../../../core/booking/booking.service';
import { Booking, ScheduleSlot } from '../../../core/booking/booking.models';
import { AiService } from '../../../core/ai/ai.service';
import { RecommendationResponse } from '../../../core/ai/ai.models';
import { BusinessService } from '../../../core/business/business.service';
import { Business, BusinessType } from '../../../core/business/business.models';

interface DisciplineHero {
  image: string;
  heading: string;
  subheading: string;
  slotsTitle: string;
}

const DISCIPLINE_HERO: Record<BusinessType, DisciplineHero> = {
  GYM: {
    image: 'https://images.pexels.com/photos/29224211/pexels-photo-29224211.jpeg?auto=compress&cs=tinysrgb&w=1600',
    heading: 'Tu gimnasio',
    subheading: 'Clases dirigidas y sala de musculación',
    slotsTitle: 'Próximas clases',
  },
  CROSSFIT_BOX: {
    image: 'https://images.pexels.com/photos/37972529/pexels-photo-37972529.jpeg?auto=compress&cs=tinysrgb&w=1600',
    heading: 'Tu box',
    subheading: 'WODs y entrenamientos del día',
    slotsTitle: 'Próximos WODs',
  },
  PADEL_CLUB: {
    image: 'https://images.pexels.com/photos/32897040/pexels-photo-32897040.jpeg?auto=compress&cs=tinysrgb&w=1600',
    heading: 'Tu club de pádel',
    subheading: 'Reserva de pistas por franja horaria',
    slotsTitle: 'Próximas franjas de pista',
  },
};

@Component({
  selector: 'app-schedule',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './schedule.component.html',
})
export class ScheduleComponent implements OnInit {
  private readonly bookingService = inject(BookingService);
  private readonly aiService = inject(AiService);
  private readonly businessService = inject(BusinessService);

  readonly slots = signal<ScheduleSlot[]>([]);
  readonly myBookings = signal<Booking[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly bookingSlotId = signal<string | null>(null);

  readonly business = signal<Business | null>(null);
  readonly hero = computed<DisciplineHero | null>(() => {
    const business = this.business();
    return business ? DISCIPLINE_HERO[business.type] : null;
  });

  readonly recommendation = signal<RecommendationResponse | null>(null);
  readonly loadingRecommendation = signal(true);

  ngOnInit(): void {
    this.reload();
    this.loadRecommendation();
    this.businessService.getMyBusiness().subscribe({
      next: (business) => this.business.set(business),
      error: () => {
        // Si no se puede resolver el negocio (p. ej. datos de demo incompletos),
        // la pagina sigue funcionando con la cabecera generica.
      },
    });
  }

  reload(): void {
    this.loading.set(true);
    this.bookingService.listSlots().subscribe({
      next: (slots) => {
        this.slots.set(slots);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
    this.bookingService.listMyBookings().subscribe((bookings) => this.myBookings.set(bookings));
  }

  loadRecommendation(): void {
    this.loadingRecommendation.set(true);
    this.aiService.recommend().subscribe({
      next: (recommendation) => {
        this.recommendation.set(recommendation);
        this.loadingRecommendation.set(false);
      },
      // El asistente de IA es un extra: si falla (p. ej. sin clave configurada),
      // el resto de la pagina de reservas debe seguir funcionando con normalidad.
      error: () => this.loadingRecommendation.set(false),
    });
  }

  activeBookingForSlot(slotId: string): Booking | undefined {
    return this.myBookings().find((b) => b.slotId === slotId && b.status !== 'CANCELLED');
  }

  book(slot: ScheduleSlot): void {
    this.bookingSlotId.set(slot.id);
    this.errorMessage.set(null);

    this.bookingService.book(slot.id).subscribe({
      next: () => {
        this.bookingSlotId.set(null);
        this.reload();
      },
      error: (err) => {
        this.bookingSlotId.set(null);
        this.errorMessage.set(err.error?.message ?? 'No se pudo reservar.');
      },
    });
  }

  cancel(booking: Booking): void {
    this.bookingService.cancelBooking(booking.id).subscribe(() => this.reload());
  }
}
