export interface CurrencySalarySummary {
  currencyCode: string;
  currencySymbol: string;
  employeeCount: number;
  minSalary: number;
  maxSalary: number;
  avgSalary: number;
  totalAnnualSpend: number;
}

export interface DashboardOverview {
  totalActiveEmployees: number;
  totalDepartments: number;
  totalCountries: number;
  activeCurrenciesCount: number;
  currencySummaries: CurrencySalarySummary[];
}

export interface DepartmentAnalytics {
  departmentId: number;
  departmentName: string;
  employeeCount: number;
  avgSalaryByCurrency: { [currencyCode: string]: number };
}

export interface CountryAnalytics {
  countryId: number;
  countryName: string;
  countryCode: string;
  employeeCount: number;
  avgSalaryByCurrency: { [currencyCode: string]: number };
}

export interface SalaryBand {
  bandName: string;
  minAmount: number;
  maxAmount: number;
  currencyCode: string;
  employeeCount: number;
}
