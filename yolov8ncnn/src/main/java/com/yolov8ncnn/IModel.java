package com.yolov8ncnn;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.function.Function;

public interface IModel<I extends IItem> {

    /***
     * 交通工具模型
     */
    Model<IItem.VehicleType> VEHICLE_MODEL = new Model<>("vehicle", IItem.VehicleType.values().length, id -> IItem.VehicleType.values()[id]);

    /***
     * 车辆牌照模型
     */
    Model<IItem.CardType> CARD_MODEL = new Model<>("card", IItem.CardType.values().length, id -> IItem.CardType.values()[id]);

    /***
     * 交通识别标志物模型
     */
    Model<IItem.TrafficSignType> TRAFFIC_SIGN_MODEL = new Model<>("traffic_sign", IItem.TrafficSignType.values().length, id -> IItem.TrafficSignType.values()[id]);

    /***
     * 红绿灯模型
     */
    Model<IItem.TrafficLightType> TRAFFIC_LIGHT_MODEL = new Model<>("traffic_light", IItem.TrafficLightType.values().length, id -> IItem.TrafficLightType.values()[id]);

    /***
     * 形状和颜色模型
     */
    Model<IItem.ShapeAndColorType> SHAPE_AND_COLOR_MODEL = new Model<>("shape_and_color", IItem.ShapeAndColorType.values().length, id -> IItem.ShapeAndColorType.values()[id]);

    /***
     * 口罩模型
     */
    Model<IItem.MaskType> MASK_MODEL = new Model<>("mask", IItem.MaskType.values().length, id -> IItem.MaskType.values()[id]);

    String getModelName();

    I asItem(int id);

    int getItemSize();

    String getExtractBlobName();

    void loadModel();

    IItem.ItemCell<I>[] detect(Bitmap bitmap);

    boolean isLoaded();


    @Getter
    class Model<I extends IItem> implements IModel<I> {

        private final String modelName;
        private final int itemSize;
        private final Function<Integer, I> asItem;

        @Setter
        @Accessors(chain = true)
        private String extractBlobName = "output0";


        @Setter
        @Accessors(chain = true)
        private boolean useGpu = true;

        private boolean loaded;

        public Model(String modelName, int itemSize, Function<Integer, I> asItem) {
            this.modelName = modelName;
            this.itemSize = itemSize;
            this.asItem = asItem;
        }


        @Override
        public I asItem(int id) {
            return asItem.apply(id);
        }

        @Override
        public void loadModel() {
            Yolov8Ncnn.loadModel(getModelName(), getItemSize(), getExtractBlobName(), isUseGpu());
            loaded = true;
        }

        @Override
        public IItem.ItemCell<I>[] detect(Bitmap bitmap) {
            if (!loaded) {
                throw new RuntimeException("model not loaded");
            }
            Yolov8Ncnn.Obj[] detect = Yolov8Ncnn.detect(bitmap, getModelName());

            //noinspection unchecked
            return Arrays.stream(detect)
                    .map(item -> new IItem.ItemCell<>(asItem.apply(item.label), new RectF(item.x, item.y, item.x + item.w, item.y + item.h), item.prob))
                    .toArray(IItem.ItemCell[]::new);
        }
    }


}
