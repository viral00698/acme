import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule],
  template: `
    <div class="audit-container">
      <h1 class="page-title">Security & Compensation Audit Logs</h1>
      <p class="page-subtitle">Immutable audit trail of employee profile and salary modifications</p>
      <mat-card class="placeholder-card">
        <mat-icon>history</mat-icon>
        <p>Audit logging view for HR Administrators.</p>
      </mat-card>
    </div>
  `,
  styles: [`
    .audit-container { max-width: 1400px; margin: 0 auto; }
    .page-title { font-size: 1.6rem; font-weight: 700; color: #0f172a; margin: 0; }
    .page-subtitle { font-size: 0.9rem; color: #64748b; margin-top: 4px; margin-bottom: 24px; }
    .placeholder-card { padding: 48px; text-align: center; color: #64748b; border-radius: 12px; }
  `]
})
export class AuditLogsComponent {}
