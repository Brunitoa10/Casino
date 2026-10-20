import { useCallback, useState } from 'react';
import { sound } from '@/lib/sound';

export function useSoundToggle() {
  const [muted, setMuted] = useState(sound.isMuted());
  const toggle = useCallback(() => {
    sound.setMuted(!muted);
    setMuted(!muted);
  }, [muted]);
  return { muted, toggle };
}
