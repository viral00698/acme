import { Department, Country, JobTitle } from './organization.model';

export type EmploymentStatus = 'ACTIVE' | 'INACTIVE' | 'TERMINATED' | 'ON_LEAVE';

export interface EmployeeSummary {
  id: number;
  employeeCode: string;
  firstName: string;
  lastName: string;
  fullName?: string;
  email: string;
  departmentName: string;
  countryName: string;
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
  department: Department;
  country: Country;
  jobTitle: JobTitle;
  status: EmploymentStatus;
  hireDate: string;
  createdAt: string;
  updatedAt: string;
  currentSalary?: {
    id: number;
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
  departmentId: number;
  countryId: number;
  jobTitleId: number;
  hireDate: string;
}

export interface UpdateEmployeeRequest {
  firstName: string;
  lastName: string;
  email: string;
  departmentId: number;
  countryId: number;
  jobTitleId: number;
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
