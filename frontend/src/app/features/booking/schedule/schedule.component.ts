import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BusinessService } from '../../../core/business/business.service';
import { BusinessType } from '../../../core/business/business.models';

const ROUTE_BY_TYPE: Record<BusinessType, string> = {
  GYM: '/gimnasio',
  CROSSFIT_BOX: '/box',
  PADEL_CLUB: '/padel',
};

/**
 * Despachador: un socio nunca se queda aqui, se le redirige de inmediato a
 * la pagina de su disciplina (gimnasio/box/padel) segun el tipo de negocio
 * al que pertenece. Mantiene estable el enlace historico "/schedule" usado
 * desde el login y el dashboard sin tener que tocar esos sitios cada vez
 * que cambie el enrutado de las paginas de disciplina.
 */
@Component({
  selector: 'app-schedule-dispatcher',
  standalone: true,
  template: '',
})
export class ScheduleComponent implements OnInit {
  private readonly businessService = inject(BusinessService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.businessService.getMyBusiness().subscribe({
      next: (business) => this.router.navigate([ROUTE_BY_TYPE[business.type]]),
      error: () => this.router.navigate(['/dashboard']),
    });
  }
}
