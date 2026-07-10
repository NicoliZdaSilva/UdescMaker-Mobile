import type { PropsWithChildren } from 'react';
import { KeyboardAvoidingView, Platform, StyleSheet, View, type ViewStyle } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { SafeAreaView } from 'react-native-safe-area-context';

import { colors } from '../theme';

interface ScreenProps extends PropsWithChildren {
  keyboard?: boolean;
  style?: ViewStyle;
}

export function Screen({ children, keyboard = false, style }: ScreenProps) {
  const content = <View style={[styles.content, style]}>{children}</View>;
  return (
    <LinearGradient
      colors={[colors.background, '#eef7ed', colors.backgroundGreen]}
      locations={[0, 0.55, 1]}
      style={styles.gradient}
    >
      <SafeAreaView edges={['top', 'left', 'right']} style={styles.safeArea}>
        {keyboard ? (
          <KeyboardAvoidingView
            behavior={Platform.OS === 'ios' ? 'padding' : undefined}
            style={styles.flex}
          >
            {content}
          </KeyboardAvoidingView>
        ) : (
          content
        )}
      </SafeAreaView>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  gradient: { flex: 1 },
  safeArea: { flex: 1 },
  content: { flex: 1, width: '100%', maxWidth: 720, alignSelf: 'center' }
});
