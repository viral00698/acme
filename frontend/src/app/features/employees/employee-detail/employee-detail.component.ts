import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { EmployeeService } from '../../../core/services/employee.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { EmployeeDetail } from '../../../core/models/employee.model';
import { EmployeeFormDialogComponent } from '../employee-form-dialog/employee-form-dialog.component';
import { StatusDialogComponent } from '../status-dialog/status-dialog.component';

@Component({
  selector: 'app-employee-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatDialogModule
  ],
  templateUrl: './employee-detail.component.html',
  styleUrls: ['./employee-detail.component.scss']
})
export class EmployeeDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private employeeService = inject(EmployeeService);
  authService = inject(AuthService);
  private notification = inject(NotificationService);
  private dialog = inject(MatDialog);

  employeeId!: number;
  employee = signal<EmployeeDetail | null>(null);
  isLoading = signal<boolean>(true);

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.employeeId = Number(idParam);
      this.loadEmployee();
    } else {
      this.router.navigate(['/employees']);
    }
  }

  loadEmployee(): void {
    this.isLoading.set(true);
    this.employeeService.getEmployee(this.employeeId).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        if (res.success && res.data) {
          this.employee.set(res.data);
        }
      },
      error: () => {
        this.isLoading.set(false);
        this.notification.error('Employee record not found');
        this.router.navigate(['/employees']);
      }
    });
  }

  editProfile(): void {
    const current = this.employee();
    if (!current) return;

    const dialogRef = this.dialog.open(EmployeeFormDialogComponent, {
      width: '600px',
      data: { isEdit: true, employee: current }
    });

    dialogRef.afterClosed().subscribe(res => {
      if (res) {
        this.loadEmployee();
      }
    });
  }

  changeStatus(): void {
    const current = this.employee();
    if (!current) return;

    const dialogRef = this.dialog.open(StatusDialogComponent, {
      width: '450px',
      data: {
        employeeId: current.id,
        employeeName: `${current.firstName} ${current.lastName}`,
        currentStatus: current.status
      }
    });

    dialogRef.afterClosed().subscribe(res => {
      if (res) {
        this.employeeService.updateStatus(current.id, res).subscribe({
          next: () => {
            this.notification.success('Status updated successfully');
            this.loadEmployee();
          },
          error: (err) => {
            this.notification.error(err.error?.message || 'Failed to update status');
          }
        });
      }
    });
  }
}
