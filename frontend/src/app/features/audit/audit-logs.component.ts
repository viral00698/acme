import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuditService, AuditFilterParams } from '../../core/services/audit.service';
import { NotificationService } from '../../core/services/notification.service';
import { AuditLog } from '../../core/models/audit.model';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './audit-logs.component.html',
  styleUrls: ['./audit-logs.component.scss']
})
export class AuditLogsComponent implements OnInit {
  private auditService = inject(AuditService);
  private notification = inject(NotificationService);
  private fb = inject(FormBuilder);

  displayedColumns: string[] = [
    'id',
    'action',
    'entityName',
    'entityId',
    'actor',
    'ipAddress',
    'details',
    'createdAt'
  ];

  auditLogs = signal<AuditLog[]>([]);
  totalElements = signal<number>(0);
  pageSize = signal<number>(20);
  pageIndex = signal<number>(0);
  isLoading = signal<boolean>(false);

  filterForm: FormGroup = this.fb.group({
    entityName: [''],
    actor: [''],
    entityId: [null]
  });

  ngOnInit(): void {
    this.loadAuditLogs();
  }

  loadAuditLogs(): void {
    this.isLoading.set(true);
    const formVals = this.filterForm.value;

    const params: AuditFilterParams = {
      entityName: formVals.entityName || undefined,
      actor: formVals.actor || undefined,
      entityId: formVals.entityId || undefined,
      page: this.pageIndex(),
      size: this.pageSize()
    };

    this.auditService.getAuditLogs(params).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        if (res.success && res.data) {
          this.auditLogs.set(res.data.content);
          this.totalElements.set(res.data.totalElements);
        }
      },
      error: () => {
        this.isLoading.set(false);
        this.notification.error('Failed to load audit logs');
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadAuditLogs();
  }

  applyFilters(): void {
    this.pageIndex.set(0);
    this.loadAuditLogs();
  }

  resetFilters(): void {
    this.filterForm.reset({
      entityName: '',
      actor: '',
      entityId: null
    });
    this.pageIndex.set(0);
    this.loadAuditLogs();
  }
}
