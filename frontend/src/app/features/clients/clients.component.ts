import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { UserResponse } from '../../core/auth/auth.models';

@Component({
  selector: 'app-clients',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './clients.component.html',
})
export class ClientsComponent implements OnInit {
  private readonly authService = inject(AuthService);

  readonly clients = signal<UserResponse[]>([]);
  readonly loading = signal(true);
  readonly busyClientId = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.authService.listClients().subscribe({
      next: (clients) => {
        this.clients.set(clients);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('No se pudieron cargar los clientes.');
        this.loading.set(false);
      },
    });
  }

  approve(client: UserResponse): void {
    this.applyUpdate(client.id, { status: 'ACTIVE' });
  }

  reject(client: UserResponse): void {
    this.applyUpdate(client.id, { status: 'REJECTED' });
  }

  togglePaid(client: UserResponse): void {
    this.applyUpdate(client.id, { paid: !client.paid });
  }

  remove(client: UserResponse): void {
    if (!confirm(`¿Eliminar a ${client.fullName} (${client.email})? Esta acción no se puede deshacer.`)) {
      return;
    }
    this.busyClientId.set(client.id);
    this.authService.deleteClient(client.id).subscribe({
      next: () => {
        this.clients.update((list) => list.filter((c) => c.id !== client.id));
        this.busyClientId.set(null);
      },
      error: () => {
        this.errorMessage.set('No se pudo eliminar el cliente.');
        this.busyClientId.set(null);
      },
    });
  }

  private applyUpdate(clientId: string, request: { status?: UserResponse['status']; paid?: boolean }): void {
    this.busyClientId.set(clientId);
    this.authService.updateClient(clientId, request).subscribe({
      next: (updated) => {
        this.clients.update((list) => list.map((c) => (c.id === clientId ? updated : c)));
        this.busyClientId.set(null);
      },
      error: () => {
        this.errorMessage.set('No se pudo actualizar el cliente.');
        this.busyClientId.set(null);
      },
    });
  }
}
