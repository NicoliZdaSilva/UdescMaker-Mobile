import { getYouTubeEmbedUrl, getYouTubeVideoId } from './youtube';

describe('YouTube seguro', () => {
  it.each([
    ['https://youtube.com/watch?v=dQw4w9WgXcQ', 'dQw4w9WgXcQ'],
    ['https://youtu.be/dQw4w9WgXcQ', 'dQw4w9WgXcQ'],
    ['https://www.youtube.com/shorts/dQw4w9WgXcQ', 'dQw4w9WgXcQ']
  ])('aceita somente os formatos HTTPS definidos no contrato', (url, id) => {
    expect(getYouTubeVideoId(url)).toBe(id);
  });

  it.each([
    'http://youtube.com/watch?v=dQw4w9WgXcQ',
    'https://youtube.com/embed/dQw4w9WgXcQ',
    'https://youtube.com/live/dQw4w9WgXcQ',
    'https://youtu.be/dQw4w9WgXcQ/outro',
    'https://youtube.com/shorts/dQw4w9WgXcQ/outro',
    'https://usuario@youtube.com/watch?v=dQw4w9WgXcQ',
    'https://music.youtube.com/watch?v=dQw4w9WgXcQ',
    'https://example.com/watch?v=dQw4w9WgXcQ'
  ])('rejeita URL fora da lista permitida: %s', (url) => {
    expect(getYouTubeVideoId(url)).toBeNull();
  });

  it('sempre gera embed no domínio sem cookies', () => {
    expect(getYouTubeEmbedUrl('https://youtu.be/dQw4w9WgXcQ')).toBe(
      'https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ?rel=0'
    );
  });
});
