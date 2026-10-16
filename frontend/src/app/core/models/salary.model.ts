export type PayFrequency = 'ANNUAL' | 'MONTHLY' | 'BIWEEKLY';

export interface SalaryRecord {
  id: number;
  employeeId: number;
  amount: number;
  currencyCode: string;
  currencySymbol: string;
  payFrequency: PayFrequency;
  effectiveFrom: string;
  effectiveTo?: string;
  changeReason?: string;
  notes?: string;
  version?: number;
  createdByName?: string;
  createdByUsername?: string;
  createdAt: string;
  active: boolean;
}

export interface SalaryHistory {
  employeeId: number;
  employeeCode?: string;
  employeeName: string;
  currentSalary?: SalaryRecord | null;
  history: SalaryRecord[];
  records?: SalaryRecord[];
}

export interface CreateSalaryRequest {
  amount: number;
  currencyCode: string;
  payFrequency: PayFrequency;
  effectiveFrom: string;
  effectiveTo?: string;
  changeReason?: string;
  notes?: string;
}

export interface SalaryReportRow {
  employeeCode: string;
  employeeName: string;
  department: string;
  country: string;
  jobTitle: string;
  status: string;
  salaryAmount: number;
  currency: string;
  payFrequency: string;
  effectiveFrom: string;
}
