package com.example.prwebmemov1.CodeHelper;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * Created by YimSB-i5 on 2020-02-05
 * SQL Query 할때 thread 로 돌리고 결과를 Handler 함수 정의 해서 메세지로 받아서 처리하는데,
 * 요거 할때 보통 WeakHandler 구성해서 하거든..
 * 근데 코드가 정해져 있고 반복되어서, 제네릭 형태로 쓰기 편하게 정리한 거.
 */
public class TaskHelper {
    public interface IWeakHandler {
        void handleMessage(Message msg);
    }

    public static class GWeakHandler<E> extends Handler {
        private final WeakReference<E> mParent;
        private final IWeakHandler mHandler;

        public GWeakHandler(E parent, IWeakHandler handler) {
            mParent = new WeakReference<>(parent);
            mHandler = handler;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            E instance = mParent.get();
            if (instance != null) {
                mHandler.handleMessage(msg);
            }
        }
    }
}
