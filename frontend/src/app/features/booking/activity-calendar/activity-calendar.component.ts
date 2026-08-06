import { CommonModule } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { Booking, ScheduleSlot } from '../../../core/booking/booking.models';

/**
 * Calendario de franjas horarias + "mis reservas", reutilizado por las 3
 * paginas de disciplina (gimnasio/box/padel): la logica de reservar/cancelar
 * es identica en las tres, solo cambia la cabecera y el contenido extra de
 * cada pagina.
 */
@Component({
  selector: 'app-activity-calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './activity-calendar.component.html',
})
export class ActivityCalendarComponent {
  readonly slots = input.required<ScheduleSlot[]>();
  readonly myBookings = input.required<Booking[]>();
  readonly bookingSlotId = input<string | null>(null);
  readonly loading = input(false);
  readonly title = input('Próximas franjas horarias');
  readonly emptyMessage = input('No hay franjas horarias disponibles todavía.');
  readonly showMyBookings = input(true);

  readonly book = output<ScheduleSlot>();
  readonly cancel = output<Booking>();

  activeBookingForSlot(slotId: string): Booking | undefined {
    return this.myBookings().find((b) => b.slotId === slotId && b.status !== 'CANCELLED');
  }
}
