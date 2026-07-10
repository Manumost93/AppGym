import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Activity, ActivityRequest, Booking, ScheduleSlot, ScheduleSlotRequest } from './booking.models';

@Injectable({ providedIn: 'root' })
export class BookingService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/booking`;

  listActivities(): Observable<Activity[]> {
    return this.http.get<Activity[]>(`${this.baseUrl}/activities`);
  }

  createActivity(request: ActivityRequest): Observable<Activity> {
    return this.http.post<Activity>(`${this.baseUrl}/activities`, request);
  }

  deactivateActivity(activityId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/activities/${activityId}`);
  }

  listSlots(): Observable<ScheduleSlot[]> {
    return this.http.get<ScheduleSlot[]>(`${this.baseUrl}/slots`);
  }

  createSlot(request: ScheduleSlotRequest): Observable<ScheduleSlot> {
    return this.http.post<ScheduleSlot>(`${this.baseUrl}/slots`, request);
  }

  book(slotId: string): Observable<Booking> {
    return this.http.post<Booking>(`${this.baseUrl}/bookings`, { slotId });
  }

  listMyBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.baseUrl}/bookings/me`);
  }

  cancelBooking(bookingId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/bookings/${bookingId}`);
  }
}
