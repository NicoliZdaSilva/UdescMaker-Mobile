import { render } from '@testing-library/react-native';

import { projectSummary } from '../test/fixtures';
import { RelatedProjects } from './RelatedProjects';
import { YouTubeEmbed } from './YouTubeEmbed';

describe('conteúdo condicional', () => {
  it('só renderiza vídeo para URL válida do YouTube', () => {
    expect(
      render(<YouTubeEmbed title="Teste" url="https://youtu.be/dQw4w9WgXcQ" />).getByTestId(
        'youtube-embed'
      )
    ).toBeTruthy();
    expect(render(<YouTubeEmbed title="Teste" url={null} />).queryByTestId('youtube-embed')).toBeNull();
    expect(
      render(<YouTubeEmbed title="Teste" url="https://example.com/video" />).queryByTestId('youtube-embed')
    ).toBeNull();
  });

  it('oculta relacionados vazios e renderiza os existentes', () => {
    expect(
      render(<RelatedProjects onOpen={jest.fn()} projects={[]} />).queryByTestId('related-projects')
    ).toBeNull();
    expect(
      render(<RelatedProjects onOpen={jest.fn()} projects={[projectSummary()]} />).getByTestId(
        'related-projects'
      )
    ).toBeTruthy();
  });
});
