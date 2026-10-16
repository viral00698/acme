export interface AuditLog {
  id: number;
  action: string;
  entityType?: string;
  entityName?: string;
  entityId: number | string;
  performedBy?: string;
  actorUsername?: string;
  actor?: string;
  performedAt?: string;
  createdAt?: string;
  ipAddress?: string;
  clientIp?: string;
  details?: string;
  detailsJson?: string;
  previousState?: string;
  newState?: string;
}
