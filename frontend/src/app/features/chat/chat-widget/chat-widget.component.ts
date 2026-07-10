import { CommonModule } from '@angular/common';
import { Component, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AiService } from '../../../core/ai/ai.service';
import { ChatMessage } from '../../../core/ai/ai.models';

@Component({
  selector: 'app-chat-widget',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-widget.component.html',
})
export class ChatWidgetComponent {
  private readonly aiService = inject(AiService);

  @ViewChild('scrollAnchor') scrollAnchor?: ElementRef<HTMLDivElement>;

  readonly open = signal(false);
  readonly messages = signal<ChatMessage[]>([
    { role: 'assistant', content: '¡Hola! Soy el asistente de AppGym. ¿En qué puedo ayudarte?' },
  ]);
  readonly draft = signal('');
  readonly sending = signal(false);

  toggle(): void {
    this.open.set(!this.open());
  }

  send(): void {
    const text = this.draft().trim();
    if (!text || this.sending()) {
      return;
    }

    const history = [...this.messages(), { role: 'user' as const, content: text }];
    this.messages.set(history);
    this.draft.set('');
    this.sending.set(true);

    this.aiService.chat(history).subscribe({
      next: (response) => {
        this.messages.set([...this.messages(), { role: 'assistant', content: response.reply }]);
        this.sending.set(false);
        this.scrollToBottom();
      },
      error: () => {
        this.messages.set([
          ...this.messages(),
          { role: 'assistant', content: 'Lo siento, no he podido responder. Inténtalo de nuevo en un momento.' },
        ]);
        this.sending.set(false);
        this.scrollToBottom();
      },
    });
  }

  private scrollToBottom(): void {
    setTimeout(() => this.scrollAnchor?.nativeElement.scrollIntoView({ behavior: 'smooth' }));
  }
}
