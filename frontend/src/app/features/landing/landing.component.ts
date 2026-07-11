import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

interface DemoAccount {
  role: string;
  email: string;
  description: string;
}

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './landing.component.html',
})
export class LandingComponent {
  readonly demoPassword = 'Demo1234!';

  readonly demoAccounts: DemoAccount[] = [
    {
      role: 'Super admin',
      email: 'admin@appgym.demo',
      description: 'Gestiona todos los negocios de la plataforma.',
    },
    {
      role: 'Administrador de negocio',
      email: 'owner@appgym.demo',
      description: 'Gestiona su box: planes, staff, actividades.',
    },
    {
      role: 'Staff',
      email: 'staff@appgym.demo',
      description: 'Gestiona actividades y horarios del negocio.',
    },
    {
      role: 'Socio',
      email: 'member@appgym.demo',
      description: 'Reserva clases y pistas, chatea con el asistente IA.',
    },
  ];

  readonly features = [
    {
      title: 'Reservas con cupo y lista de espera',
      description:
        'Clases de crossfit, gimnasio o pistas de pádel: control de aforo en tiempo real y lista de espera automática.',
    },
    {
      title: 'Multi-negocio desde el primer día',
      description:
        'Un mismo modelo de datos sirve para gimnasios, boxes de crossfit y clubes de pádel, con roles y permisos por negocio.',
    },
    {
      title: 'Asistente con IA',
      description:
        'Chatbot, recomendador de actividades e insights de negocio impulsados por la API de Claude.',
    },
    {
      title: 'Arquitectura de microservicios',
      description:
        'Angular + 5 microservicios Spring Boot tras un API Gateway con JWT, Docker y CI/CD a GHCR.',
    },
  ];
}
