export type BusinessType = 'GYM' | 'CROSSFIT_BOX' | 'PADEL_CLUB';

export interface Business {
  id: string;
  name: string;
  type: BusinessType;
  description: string | null;
  contactEmail: string | null;
  contactPhone: string | null;
  address: string | null;
  primaryColor: string | null;
  active: boolean;
}

export interface CreateBusinessRequest {
  name: string;
  type: BusinessType;
  description?: string;
  contactEmail?: string;
  contactPhone?: string;
  address?: string;
  primaryColor?: string;
}

export interface MembershipPlan {
  id: string;
  businessId: string;
  name: string;
  description: string | null;
  priceCents: number;
  currency: string;
  durationDays: number;
  active: boolean;
}

export interface MembershipPlanRequest {
  name: string;
  description?: string;
  priceCents: number;
  currency: string;
  durationDays: number;
}
