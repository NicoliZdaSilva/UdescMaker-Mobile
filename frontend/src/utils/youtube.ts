const VIDEO_ID_PATTERN = /^[A-Za-z0-9_-]{11}$/;

export function getYouTubeVideoId(value?: string | null): string | null {
  if (!value) return null;

  try {
    const url = new URL(value.trim());
    if (url.protocol !== 'https:' || url.username || url.password) return null;

    const hostname = url.hostname.toLowerCase().replace(/^www\./, '');
    const path = url.pathname.split('/').filter(Boolean);
    let id: string | null = null;

    if (hostname === 'youtu.be' && path.length === 1) {
      id = path[0] ?? null;
    } else if (hostname === 'youtube.com' || hostname === 'm.youtube.com') {
      if (url.pathname === '/watch') id = url.searchParams.get('v');
      else if (path[0] === 'shorts' && path.length === 2) id = path[1] ?? null;
    }

    return id && VIDEO_ID_PATTERN.test(id) ? id : null;
  } catch {
    return null;
  }
}

export function getYouTubeEmbedUrl(value?: string | null) {
  const id = getYouTubeVideoId(value);
  return id ? `https://www.youtube-nocookie.com/embed/${id}?rel=0` : null;
}
