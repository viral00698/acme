import { Department, Country, JobTitle } from './organization.model';

export type EmploymentStatus = 'ACTIVE' | 'INACTIVE' | 'TERMINATED' | 'ON_LEAVE';

export interface EmployeeSummary {
  id: number;
  employeeCode: string;
  firstName: string;
  lastName: string;
  fullName?: string;
  email: string;
  phoneNumber?: string;
  departmentId?: number;
  departmentName: string;
  countryId?: number;
  countryName: string;
  countryCode?: string;
  jobTitleId?: number;
  jobTitle: string;
  status: EmploymentStatus;
  hireDate: string;
}

export interface EmployeeDetail {
  id: number;
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  departmentId?: number;
  departmentName?: string;
  countryId?: number;
  countryName?: string;
  countryCode?: string;
  jobTitleId?: number;
  jobTitle?: string | JobTitle;
  department?: Department | { id?: number; name: string; departmentCode?: string; code?: string };
  country?: Country | { id?: number; name: string; isoCode?: string; code?: string };
  status: EmploymentStatus;
  hireDate: string;
  createdAt: string;
  updatedAt: string;
  currentSalaryAmount?: number;
  currentSalaryCurrency?: string;
  currentSalarySymbol?: string;
  currentPayFrequency?: string;
  currentSalaryEffectiveFrom?: string;
  currentSalary?: {
    id?: number;
    amount: number;
    currencyCode: string;
    currencySymbol: string;
    payFrequency: string;
    effectiveFrom: string;
    effectiveTo?: string;
    changeReason?: string;
    notes?: string;
  };
}

export interface CreateEmployeeRequest {
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  departmentId: number;
  countryId: number;
  jobTitleId: number;
  hireDate: string;
}

export interface UpdateEmployeeRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  departmentId: number;
  countryId: number;
  jobTitleId: number;
  hireDate?: string;
}

export interface UpdateStatusRequest {
  status: EmploymentStatus;
  reason?: string;
}

export interface EmployeeSearchParams {
  search?: string;
  departmentId?: number | null;
  countryId?: number | null;
  jobTitleId?: number | null;
  status?: EmploymentStatus | null;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}
