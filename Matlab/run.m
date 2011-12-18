%% Setup
clear ; close all; clc;
[data,colheaders] = importfile('..\target\facts.csv');
m = size(data,1);
X = data(:,2:end);
Y = ~data(:,1);

[Z,mu,sigma] = zscore(X(:,[56;78]));
XNorm = X;
XNorm(:,[56;78]) = Z(:,1:2);
XNorm = [ones(m,1) XNorm];

cvpart = cvpartition(Y,'holdout',0.3);
Xtrain = XNorm(training(cvpart),:);
Ytrain = Y(training(cvpart),:);
Xtest = XNorm(test(cvpart),:);
Ytest = Y(test(cvpart),:);

[theta] = trainModel(Xtrain, Ytrain, 1);
[f1 p r] = evalResults(sigmoid(theta' * Xtest')', Ytest, .3)
xlswrite('foo.xls',[colheaders' num2cell(theta)]);

%%
% lambda = 15;
% [error_train, error_val] = ... % [ones(size(Xtrain, 1), 1)
%     learningCurve(Xtrain, Ytrain, ...
%     Xtest, Ytest, ... % [ones(size(Xtest, 1), 1)
%     lambda);
% 
% mtrain = size(Xtrain, 1)
% plot(1:mtrain, error_train, 1:mtrain, error_val);
% title('Learning curve for linear regression')
% legend('Train', 'Cross Validation')
% xlabel('Number of training examples')
% ylabel('Error')
% axis([0 13 0 150 500])
% 
% fprintf('# Training Examples\tTrain Error\tCross Validation Error\n');
% for i = 1:mtrain
%     fprintf('  \t%d\t\t%f\t%f\n', i, error_train(i), error_val(i));
% end


%%

% [lambda_vec, error_train, error_val] = validationCurve(Xtrain, Ytrain, Xtest, Ytest);
% close all;
% plot(lambda_vec, error_train, lambda_vec, error_val);
% legend('Train', 'Cross Validation');
% xlabel('lambda');
% ylabel('Error');
% fprintf('lambda\t\tTrain Error\tValidation Error\n');
% for i = 1:length(lambda_vec)
% 	fprintf(' %f\t%f\t%f\n', ...
%             lambda_vec(i), error_train(i), error_val(i));
% end

%%
