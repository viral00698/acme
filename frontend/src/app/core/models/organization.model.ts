export interface Department {
  id: number;
  code?: string;
  departmentCode?: string;
  name: string;
  description?: string;
  employeeCount?: number;
}

export interface Country {
  id: number;
  code?: string;
  isoCode?: string;
  name: string;
  defaultCurrencyCode?: string;
  region?: string;
}

export interface Currency {
  id: number;
  currencyCode: string;
  name: string;
  symbol: string;
}

export interface JobTitle {
  id: number;
  title: string;
  departmentId?: number;
  departmentName?: string;
}
