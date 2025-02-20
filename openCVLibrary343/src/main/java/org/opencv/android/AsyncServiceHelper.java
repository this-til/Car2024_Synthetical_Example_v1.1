package org.opencv.android;

import java.io.File;
import java.util.StringTokenizer;

import org.opencv.core.Core;
import org.opencv.engine.OpenCVEngineInterface;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * AsyncServiceHelper 类用于在 Android 应用中异步初始化 OpenCV 库。
 * 主要功能包括：
 * 1. 尝试绑定 OpenCV Manager 服务，检查服务版本。
 * 2. 获取库文件路径和依赖列表，并动态加载 native 库。
 * 3. 在必要时通过 Google Play 商店触发服务或库的安装。
 */
class AsyncServiceHelper
{
    /**
     * 静态方法 initOpenCV 发起 OpenCV 初始化操作
     * @param Version OpenCV 版本号
     * @param AppContext 当前应用的 Context
     * @param Callback 用于回调加载结果的接口
     * @return 如果绑定服务成功返回 true，否则触发安装后返回 false
     */
    public static boolean initOpenCV(String Version, final Context AppContext,
                                     final LoaderCallbackInterface Callback)
    {
        // 构造一个 AsyncServiceHelper 实例
        AsyncServiceHelper helper = new AsyncServiceHelper(Version, AppContext, Callback);
        // 创建用于绑定 OpenCV Manager 服务的 Intent
        Intent intent = new Intent("org.opencv.engine.BIND");
        intent.setPackage("org.opencv.engine");
        // 尝试绑定服务
        if (AppContext.bindService(intent, helper.mServiceConnection, Context.BIND_AUTO_CREATE))
        {
            return true;
        }
        else
        {
            // 如果绑定失败则取消绑定并发起安装服务操作
            AppContext.unbindService(helper.mServiceConnection);
            InstallService(AppContext, Callback);
            return false;
        }
    }

    /**
     * 构造函数
     * @param Version OpenCV 版本号
     * @param AppContext 当前应用的 Context
     * @param Callback 加载回调接口
     */
    protected AsyncServiceHelper(String Version, Context AppContext, LoaderCallbackInterface Callback)
    {
        mOpenCVersion = Version;
        mUserAppCallback = Callback;
        mAppContext = AppContext;
    }

    // 日志标签
    protected static final String TAG = "OpenCVManager/Helper";
    // OpenCV Manager 最低版本要求
    protected static final int MINIMUM_ENGINE_VERSION = 2;
    // 远程服务接口，用于与 OpenCV Manager 通信
    protected OpenCVEngineInterface mEngineService;
    // 用户应用传入的回调接口，用于通知加载状态
    protected LoaderCallbackInterface mUserAppCallback;
    // OpenCV 版本号
    protected String mOpenCVersion;
    // 应用上下文
    protected Context mAppContext;
    // 标识服务安装或库安装是否正在进行
    protected static boolean mServiceInstallationProgress = false;
    protected static boolean mLibraryInstallationProgress = false;

    /**
     * 静态方法，静默方式启动 OpenCV Manager 安装（通过 Google Play）
     * @param context 应用上下文
     * @return 安装是否成功启动
     */
    protected static boolean InstallServiceQuiet(Context context)
    {
        boolean result = true;
        try
        {
            // 构造跳转到 OpenCV Manager 在市场中的页面的 Intent
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(OPEN_CV_SERVICE_URL));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        catch(Exception e)
        {
            result = false;
        }
        return result;
    }

    /**
     * 发起 OpenCV Manager 服务安装请求
     * @param AppContext 当前应用上下文
     * @param Callback 回调接口
     */
    protected static void InstallService(final Context AppContext, final LoaderCallbackInterface Callback)
    {
        // 如果当前没有安装进程在进行，则发起新的安装请求
        if (!mServiceInstallationProgress)
        {
            Log.d(TAG, "Request new service installation");
            // 构造安装回调接口，用于通知安装结果
            InstallCallbackInterface InstallQuery = new InstallCallbackInterface() {
                private LoaderCallbackInterface mUserAppCallback = Callback;
                public String getPackageName()
                {
                    return "OpenCV Manager";
                }
                public void install() {
                    Log.d(TAG, "Trying to install OpenCV Manager via Google Play");

                    boolean result = InstallServiceQuiet(AppContext);
                    if (result)
                    {
                        mServiceInstallationProgress = true;
                        Log.d(TAG, "Package installation started");
                    }
                    else
                    {
                        Log.d(TAG, "OpenCV package was not installed!");
                        int Status = LoaderCallbackInterface.MARKET_ERROR;
                        Log.d(TAG, "Init finished with status " + Status);
                        Log.d(TAG, "Unbind from service");
                        Log.d(TAG, "Calling using callback");
                        mUserAppCallback.onManagerConnected(Status);
                    }
                }

                public void cancel()
                {
                    Log.d(TAG, "OpenCV library installation was canceled");
                    int Status = LoaderCallbackInterface.INSTALL_CANCELED;
                    Log.d(TAG, "Init finished with status " + Status);
                    Log.d(TAG, "Calling using callback");
                    mUserAppCallback.onManagerConnected(Status);
                }

                public void wait_install()
                {
                    Log.e(TAG, "Installation was not started! Nothing to wait!");
                }
            };

            // 通过回调通知用户需要进行新的安装操作
            Callback.onPackageInstall(InstallCallbackInterface.NEW_INSTALLATION, InstallQuery);
        }
        else
        {
            // 如果已有安装进程在进行，则构造等待回调接口
            Log.d(TAG, "Waiting current installation process");
            InstallCallbackInterface WaitQuery = new InstallCallbackInterface() {
                private LoaderCallbackInterface mUserAppCallback = Callback;
                public String getPackageName()
                {
                    return "OpenCV Manager";
                }
                public void install()
                {
                    Log.e(TAG, "Nothing to install we just wait current installation");
                }
                public void cancel()
                {
                    Log.d(TAG, "Waiting for OpenCV canceled by user");
                    mServiceInstallationProgress = false;
                    int Status = LoaderCallbackInterface.INSTALL_CANCELED;
                    Log.d(TAG, "Init finished with status " + Status);
                    Log.d(TAG, "Calling using callback");
                    mUserAppCallback.onManagerConnected(Status);
                }
                public void wait_install()
                {
                    InstallServiceQuiet(AppContext);
                }
            };

            Callback.onPackageInstall(InstallCallbackInterface.INSTALLATION_PROGRESS, WaitQuery);
        }
    }

    /**
     * OpenCV Manager 在 Google Play 市场中的 URL 地址
     */
    protected static final String OPEN_CV_SERVICE_URL = "market://details?id=org.opencv.engine";

    /**
     * ServiceConnection 对象，管理与 OpenCV Manager 服务的连接状态
     */
    protected ServiceConnection mServiceConnection = new ServiceConnection()
    {
        /**
         * 当服务连接成功时调用
         * @param className 服务组件名
         * @param service 绑定成功返回的 IBinder 对象
         */
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            Log.d(TAG, "Service connection created");
            // 获取远程服务接口
            mEngineService = OpenCVEngineInterface.Stub.asInterface(service);
            if (null == mEngineService)
            {
                Log.d(TAG, "OpenCV Manager Service connection fails. May be service was not installed?");
                // 如果获取失败，则发起服务安装
                InstallService(mAppContext, mUserAppCallback);
            }
            else
            {
                mServiceInstallationProgress = false;
                try
                {
                    // 检查 OpenCV Manager 的版本是否符合最低要求
                    if (mEngineService.getEngineVersion() < MINIMUM_ENGINE_VERSION)
                    {
                        Log.d(TAG, "Init finished with status " + LoaderCallbackInterface.INCOMPATIBLE_MANAGER_VERSION);
                        Log.d(TAG, "Unbind from service");
                        mAppContext.unbindService(mServiceConnection);
                        Log.d(TAG, "Calling using callback");
                        mUserAppCallback.onManagerConnected(LoaderCallbackInterface.INCOMPATIBLE_MANAGER_VERSION);
                        return;
                    }

                    Log.d(TAG, "Trying to get library path");
                    // 获取对应版本的 OpenCV 库的安装路径
                    String path = mEngineService.getLibPathByVersion(mOpenCVersion);
                    if ((null == path) || (path.length() == 0))
                    {
                        // 如果库路径为空，说明库未安装，则触发库安装流程
                        if (!mLibraryInstallationProgress)
                        {
                            InstallCallbackInterface InstallQuery = new InstallCallbackInterface() {
                                public String getPackageName()
                                {
                                    return "OpenCV library";
                                }
                                public void install() {
                                    Log.d(TAG, "Trying to install OpenCV lib via Google Play");
                                    try
                                    {
                                        // 通过服务接口发起库安装
                                        if (mEngineService.installVersion(mOpenCVersion))
                                        {
                                            mLibraryInstallationProgress = true;
                                            Log.d(TAG, "Package installation started");
                                            Log.d(TAG, "Unbind from service");
                                            mAppContext.unbindService(mServiceConnection);
                                        }
                                        else
                                        {
                                            Log.d(TAG, "OpenCV package was not installed!");
                                            Log.d(TAG, "Init finished with status " + LoaderCallbackInterface.MARKET_ERROR);
                                            Log.d(TAG, "Unbind from service");
                                            mAppContext.unbindService(mServiceConnection);
                                            Log.d(TAG, "Calling using callback");
                                            mUserAppCallback.onManagerConnected(LoaderCallbackInterface.MARKET_ERROR);
                                        }
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                        Log.d(TAG, "Init finished with status " + LoaderCallbackInterface.INIT_FAILED);
                                        Log.d(TAG, "Unbind from service");
                                        mAppContext.unbindService(mServiceConnection);
                                        Log.d(TAG, "Calling using callback");
                                        mUserAppCallback.onManagerConnected(LoaderCallbackInterface.INIT_FAILED);
                                    }
                                }
                                public void cancel() {
                                    Log.d(TAG, "OpenCV library installation was canceled");
                                    Log.d(TAG, "Init finished with status " + LoaderCallbackInterface.INSTALL_CANCELED);
                                    Log.d(TAG, "Unbind from service");
                                    mAppContext.unbindService(mServiceConnection);
                                    Log.d(TAG, "Calling using callback");
                                    mUserAppCallback.onManagerConnected(LoaderCallbackInterface.INSTALL_CANCELED);
                                }
                                public void wait_install() {
                                    Log.e(TAG, "Installation was not started! Nothing to wait!");
                                }
                            };

                            mUserAppCallback.onPackageInstall(InstallCallbackInterface.NEW_INSTALLATION, InstallQuery);
                        }
                        else
                        {
                            // 如果库安装已经在进行，则构造等待安装的回调接口
                            InstallCallbackInterface WaitQuery = new InstallCallbackInterface() {
                                public String getPackageName()
                                {
                                    return "OpenCV library";
                                }

                                public void install() {
                                    Log.e(TAG, "Nothing to install we just wait current installation");
                                }
                                public void cancel()
                                {
                                    Log.d(TAG, "OpenCV library installation was canceled");
                                    mLibraryInstallationProgress = false;
                                    Log.d(TAG, "Init finished with status " + LoaderCallbackInterface.INSTALL_CANCELED);
                                    Log.d(TAG, "Unbind from service");
                                    mAppContext.unbindService(mServiceConnection);
                                    Log.d(TAG, "Calling using callback");
                                    mUserAppCallback.onManagerConnected(LoaderCallbackInterface.INSTALL_CANCELED);
                                }
                                public void wait_install() {
                                    Log.d(TAG, "Waiting for current installation");
                                    try
                                    {
                                        // 继续等待当前库安装
                                        if (!mEngineService.installVersion(mOpenCVersion))
                                        {
                                            Log.d(TAG, "OpenCV package was not installed!");
                                            Log.d(TAG, "Init finished with status " + LoaderCallbackInterface.MARKET_ERROR);
                                            Log.d(TAG, "Calling using callback");
                                            mUserAppCallback.onManagerConnected(LoaderCallbackInterface.MARKET_ERROR);
                                        }
                                        else
                                        {
                                            Log.d(TAG, "Wating for package installation");
                                        }

                                        Log.d(TAG, "Unbind from service");
                                        mAppContext.unbindService(mServiceConnection);

                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                        Log.d(TAG, "Init finished with status " + LoaderCallbackInterface.INIT_FAILED);
                                        Log.d(TAG, "Unbind from service");
                                        mAppContext.unbindService(mServiceConnection);
                                        Log.d(TAG, "Calling using callback");
                                        mUserAppCallback.onManagerConnected(LoaderCallbackInterface.INIT_FAILED);
                                    }
                                }
                            };

                            mUserAppCallback.onPackageInstall(InstallCallbackInterface.INSTALLATION_PROGRESS, WaitQuery);
                        }
                        return;
                    }
                    else
                    {
                        // 如果库路径有效，则继续加载本地库
                        Log.d(TAG, "Trying to get library list");
                        mLibraryInstallationProgress = false;
                        // 获取库依赖列表（可能包含多个库，分号分隔）
                        String libs = mEngineService.getLibraryList(mOpenCVersion);
                        Log.d(TAG, "Library list: \"" + libs + "\"");
                        Log.d(TAG, "First attempt to load libs");
                        int status;
                        // 调用 initOpenCVLibs 方法加载 native 库
                        if (initOpenCVLibs(path, libs))
                        {
                            Log.d(TAG, "First attempt to load libs is OK");
                            // 输出 OpenCV 编译信息
                            String eol = System.getProperty("line.separator");
                            for (String str : Core.getBuildInformation().split(eol))
                                Log.i(TAG, str);

                            status = LoaderCallbackInterface.SUCCESS;
                        }
                        else
                        {
                            Log.d(TAG, "First attempt to load libs fails");
                            status = LoaderCallbackInterface.INIT_FAILED;
                        }

                        Log.d(TAG, "Init finished with status " + status);
                        Log.d(TAG, "Unbind from service");
                        mAppContext.unbindService(mServiceConnection);
                        Log.d(TAG, "Calling using callback");
                        // 回调通知应用 OpenCV 初始化结果
                        mUserAppCallback.onManagerConnected(status);
                    }
                }
                catch (RemoteException e)
                {
                    e.printStackTrace();
                    Log.d(TAG, "Init finished with status " + LoaderCallbackInterface.INIT_FAILED);
                    Log.d(TAG, "Unbind from service");
                    mAppContext.unbindService(mServiceConnection);
                    Log.d(TAG, "Calling using callback");
                    mUserAppCallback.onManagerConnected(LoaderCallbackInterface.INIT_FAILED);
                }
            }
        };

        /**
         * 当服务断开连接时调用
         * @param className 服务组件名
         */
        public void onServiceDisconnected(ComponentName className)
        {
            mEngineService = null;
        }
    };

    /**
     * 通过 System.load 动态加载指定路径的 native 库
     * @param AbsPath 库文件的绝对路径
     * @return 加载是否成功
     */
    private boolean loadLibrary(String AbsPath)
    {
        boolean result = true;

        Log.d(TAG, "Trying to load library " + AbsPath);
        try
        {
            System.load(AbsPath);
            Log.d(TAG, "OpenCV libs init was ok!");
        }
        catch(UnsatisfiedLinkError e)
        {
            Log.d(TAG, "Cannot load library \"" + AbsPath + "\"");
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    /**
     * 根据传入的库路径和库依赖列表加载所有 OpenCV native 库
     * @param Path 库文件所在目录
     * @param Libs 库文件列表（以分号分隔）
     * @return 所有库加载是否成功
     */
    private boolean initOpenCVLibs(String Path, String Libs)
    {
        Log.d(TAG, "Trying to init OpenCV libs");
        if ((null != Path) && (Path.length() != 0))
        {
            boolean result = true;
            if ((null != Libs) && (Libs.length() != 0))
            {
                Log.d(TAG, "Trying to load libs by dependency list");
                // 使用 StringTokenizer 分割库文件列表
                StringTokenizer splitter = new StringTokenizer(Libs, ";");
                while(splitter.hasMoreTokens())
                {
                    String AbsLibraryPath = Path + File.separator + splitter.nextToken();
                    // 逐个加载库文件，若加载失败则 result 为 false
                    result &= loadLibrary(AbsLibraryPath);
                }
            }
            else
            {
                // 如果没有依赖列表，则尝试加载默认的库文件
                String AbsLibraryPath = Path + File.separator + "libopencv_java3.so";
                result = loadLibrary(AbsLibraryPath);
            }
            return result;
        }
        else
        {
            Log.d(TAG, "Library path \"" + Path + "\" is empty");
            return false;
        }
    }
}
