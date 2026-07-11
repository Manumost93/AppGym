import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

interface DemoAccount {
  role: string;
  email: string;
  description: string;
}

interface Discipline {
  name: string;
  tag: string;
  description: string;
  image: string;
  businessId: string;
}

interface Feature {
  title: string;
  description: string;
  icon: string;
}

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './landing.component.html',
})
export class LandingComponent {
  readonly demoPassword = 'Demo1234!';

  readonly heroImage =
    'https://images.pexels.com/photos/29224211/pexels-photo-29224211.jpeg?auto=compress&cs=tinysrgb&w=1920';

  readonly disciplines: Discipline[] = [
    {
      name: 'Gimnasios',
      tag: 'Sala de musculación',
      description: 'Control de accesos, planes de membresía y reservas de clases dirigidas.',
      image: 'https://images.pexels.com/photos/29224211/pexels-photo-29224211.jpeg?auto=compress&cs=tinysrgb&w=900',
      businessId: '11111111-1111-1111-1111-111111111112',
    },
    {
      name: 'Boxes de crossfit',
      tag: 'WOD del día',
      description: 'Clases con cupo limitado, lista de espera automática y seguimiento de coaches.',
      image: 'https://images.pexels.com/photos/37972529/pexels-photo-37972529.jpeg?auto=compress&cs=tinysrgb&w=900',
      businessId: '11111111-1111-1111-1111-111111111111',
    },
    {
      name: 'Clubes de pádel',
      tag: 'Reserva de pistas',
      description: 'Reserva de pistas por franja horaria con confirmación al instante.',
      image: 'https://images.pexels.com/photos/32897040/pexels-photo-32897040.jpeg?auto=compress&cs=tinysrgb&w=900',
      businessId: '11111111-1111-1111-1111-111111111113',
    },
  ];

  readonly features: Feature[] = [
    {
      title: 'Reservas con cupo y lista de espera',
      description:
        'Clases de crossfit, gimnasio o pistas de pádel: control de aforo en tiempo real y lista de espera automática.',
      icon: 'calendar',
    },
    {
      title: 'Multi-negocio desde el primer día',
      description:
        'Un mismo modelo de datos sirve para gimnasios, boxes de crossfit y clubes de pádel, con roles y permisos por negocio.',
      icon: 'building',
    },
    {
      title: 'Asistente con IA',
      description:
        'Chatbot, recomendador de actividades e insights de negocio impulsados por la API de Claude.',
      icon: 'spark',
    },
    {
      title: 'Arquitectura de microservicios',
      description:
        'Angular + 5 microservicios Spring Boot tras un API Gateway con JWT, Docker y CI/CD a GHCR.',
      icon: 'layers',
    },
  ];

  readonly demoAccounts: DemoAccount[] = [
    {
      role: 'Super admin',
      email: 'admin@appgym.demo',
      description: 'Gestiona todos los negocios de la plataforma.',
    },
    {
      role: 'Administrador de negocio',
      email: 'owner@appgym.demo',
      description: 'Gestiona su box de crossfit: planes, clientes, actividades.',
    },
    {
      role: 'Administrador de gimnasio',
      email: 'gym-owner@appgym.demo',
      description: 'Gestiona el gimnasio: planes, clientes, clases.',
    },
    {
      role: 'Administrador de pádel',
      email: 'padel-owner@appgym.demo',
      description: 'Gestiona el club de pádel: planes, clientes, pistas.',
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
}
