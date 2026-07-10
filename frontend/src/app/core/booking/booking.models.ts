export type ActivityType = 'CLASS' | 'COURT';
export type BookingStatus = 'CONFIRMED' | 'WAITLIST' | 'CANCELLED';

export interface Activity {
  id: string;
  businessId: string;
  type: ActivityType;
  name: string;
  description: string | null;
  capacity: number;
  durationMinutes: number;
  instructorName: string | null;
  active: boolean;
}

export interface ActivityRequest {
  type: ActivityType;
  name: string;
  description?: string;
  capacity: number;
  durationMinutes: number;
  instructorName?: string;
}

export interface ScheduleSlot {
  id: string;
  activityId: string;
  activityName: string;
  activityType: ActivityType;
  instructorName: string | null;
  startTime: string;
  endTime: string;
  capacity: number;
  confirmedCount: number;
  waitlistCount: number;
  full: boolean;
}

export interface ScheduleSlotRequest {
  activityId: string;
  startTime: string;
}

export interface Booking {
  id: string;
  slotId: string;
  activityName: string;
  slotStartTime: string | null;
  memberId: string;
  status: BookingStatus;
  createdAt: string;
}
