%% vgg / caffe spec
%%use_gpu=0 if we are on a virtual machine
%%Import to can use caffe if u dont have on path
addpath('/home/bryan/caffe/matlab/caffe');
addpath('/home/bryan/caffe/tools');
use_gpu = 1;
caffe('set_device', 1);
%%Path to model an def fille
model_def_file = '/home/bryan/neuraltalk/matlab_features_reference/deploy_features.prototxt';
model_file = '/home/bryan/Proyecto-Fin-de-Grado/servidor/uploads/VGG_ILSVRC_16_layers.caffemodel';
batch_size = 10;

matcaffe_init(use_gpu, model_def_file, model_file);

%% input files spec

root_path = '/home/bryan/Proyecto-Fin-de-Grado/servidor/uploads/';
fs = textread([root_path 'tasks.txt'], '%s');
N = length(fs);

%%

% iterate over the iamges in batches
feats = zeros(4096, N, 'single');
for b=1:batch_size:N

    % enter images, and dont go out of bounds
    Is = {};
    for i = b:min(N,b+batch_size-1)
        I = imread([root_path fs{i}]);
        if ndims(I) == 2
            I = cat(3, I, I, I); % handle grayscale edge case. Annoying!
        end
        Is{end+1} = I;
    end
    input_data = prepare_images_batch(Is);

    tic;
    scores = caffe('forward', {input_data});
    scores = squeeze(scores{1});
    tt = toc;

    nb = length(Is);
    feats(:, b:b+nb-1) = scores(:,1:nb);
    fprintf('%d/%d = %.2f%% done in %.2fs\n', b, N, 100*(b-1)/N, tt);
end

%% write to file

save([root_path 'vgg_feats_hdf5.mat'], 'feats', '-v7.3');
save([root_path 'vgg_feats.mat'], 'feats');
