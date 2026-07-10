import { StyleSheet, View } from 'react-native';
import { WebView } from 'react-native-webview';

import { colors, radii } from '../theme';
import { getYouTubeEmbedUrl } from '../utils/youtube';

interface YouTubeEmbedProps {
  url?: string | null;
  title: string;
}

export function YouTubeEmbed({ url, title }: YouTubeEmbedProps) {
  const embedUrl = getYouTubeEmbedUrl(url);
  if (!embedUrl) return null;

  return (
    <View accessibilityLabel={`Vídeo: ${title}`} style={styles.frame} testID="youtube-embed">
      <WebView
        allowsFullscreenVideo
        javaScriptEnabled
        mediaPlaybackRequiresUserAction
        originWhitelist={['https://www.youtube-nocookie.com']}
        setSupportMultipleWindows={false}
        source={{ uri: embedUrl }}
        style={styles.webview}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  frame: {
    width: '100%',
    aspectRatio: 16 / 9,
    overflow: 'hidden',
    borderRadius: radii.md,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: '#000'
  },
  webview: { flex: 1, backgroundColor: '#000' }
});
