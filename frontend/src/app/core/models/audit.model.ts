export interface AuditLog {
  id: number;
  action: string;
  entityType: string;
  entityId: string;
  performedBy: string;
  performedAt: string;
  ipAddress?: string;
  details?: string;
  previousState?: string;
  newState?: string;
}
